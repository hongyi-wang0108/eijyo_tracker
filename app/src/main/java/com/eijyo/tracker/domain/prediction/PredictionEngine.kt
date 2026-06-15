package com.eijyo.tracker.domain.prediction

import android.content.Context
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.ConfidenceLevel
import com.eijyo.tracker.data.model.DateRange
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.PublicDataDoc
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.staticdata.PublicData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Rule-based, explainable prediction (PM doc §14 + 6.3.5). It produces three result
 * windows plus a confidence level and the reasons behind them — never a single exact
 * date, never a hidden model.
 *
 * Predictions only exist while [ApplicationStatus.REVIEWING]; if the submitted date is
 * unknown ([DatePrecision.UNKNOWN]) no review window can be computed and this returns
 * null so the UI can prompt the user to fill in the date.
 */
class PredictionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun predict(
        profile: ApplicationProfile,
        publicData: PublicData,
        today: LocalDate = LocalDate.now(),
    ): Prediction? {
        if (profile.status != ApplicationStatus.REVIEWING) return null
        val submitted = DateLabels.resolveSubmitted(
            profile.submittedDate,
            profile.submittedDatePrecision,
        ) ?: return null

        val reasons = mutableListOf<String>()

        val baseMin = publicData.standardProcessingMinMonths
        val baseMax = publicData.standardProcessingMaxMonths
        reasons += "官方标准处理期间约 ${publicData.standardProcessingRange}（${publicData.sourceName}）"

        val officeBuffer = officeBuffer(profile.submittedOffice).also {
            if (it > 0) reasons += "${context.getString((profile.submittedOffice ?: ImmigrationOffice.OTHER).labelRes)}受理量较大，保守区间相应延长"
        }

        val hasSupplement = profile.hasSupplementRequest == TriState.YES
        val supplementMinBuffer = if (hasSupplement) 1 else 0
        val supplementMaxBuffer = if (hasSupplement) 2 else 0
        if (hasSupplement) reasons += "存在补资料请求，整体处理时间通常相应延后"

        val adjMin = baseMin + supplementMinBuffer
        val adjMax = baseMax + officeBuffer + supplementMaxBuffer

        val optimistic = DateRange(
            startLabel = DateLabels.monthThird(submitted.plusMonths(adjMin.toLong()).minusDays(10)),
            endLabel = DateLabels.monthThird(submitted.plusMonths(adjMin.toLong()).plusDays(10)),
        )
        val normal = DateRange(
            startLabel = DateLabels.monthThird(submitted.plusMonths(adjMin.toLong())),
            endLabel = DateLabels.monthThird(submitted.plusMonths(adjMax.toLong())),
        )
        val conservative = DateRange(
            startLabel = DateLabels.monthThird(submitted.plusMonths(adjMax.toLong())),
            endLabel = DateLabels.monthThird(submitted.plusMonths((adjMax + 2).toLong())),
        )

        val confidence = confidence(profile.submittedDatePrecision, hasSupplement, profile.submittedOffice)
        if (profile.submittedDatePrecision == DatePrecision.MONTH) {
            reasons += "申请日期仅精确到月，预测以该月中旬估算，置信度相应降低"
        }

        val waitDays = ChronoUnit.DAYS.between(submitted, today).toInt().coerceAtLeast(0)
        val centerDays = (adjMin + adjMax) * 30 / 2
        val progress = if (centerDays <= 0) 0 else min(99, waitDays * 100 / centerDays)

        return Prediction(
            applicationId = profile.id,
            optimisticRange = optimistic.display,
            normalRange = normal.display,
            conservativeRange = conservative.display,
            confidenceLevel = confidence,
            currentWaitDays = waitDays,
            progressPercent = progress,
            reasons = reasons,
        )
    }

    /**
     * FIFO model driven by real [PublicDataDoc] data. Falls back to the old 4-6 month
     * logic (with LOW confidence) when the office has no regional series — OTHER, an
     * unknown office, or an empty `monthly` list.
     */
    fun predict(
        profile: ApplicationProfile,
        publicDataDoc: PublicDataDoc,
        today: LocalDate = LocalDate.now(),
    ): Prediction? {
        if (profile.status != ApplicationStatus.REVIEWING) return null
        val submitted = DateLabels.resolveSubmitted(
            profile.submittedDate,
            profile.submittedDatePrecision,
        ) ?: return null

        val office = profile.submittedOffice
        val series = office
            ?.takeIf { it != ImmigrationOffice.OTHER }
            ?.let { publicDataDoc.officeData(it.name) }
            ?.monthly
            .orEmpty()

        // No regional data → old standard-period logic, flagged low confidence.
        if (series.isEmpty()) {
            val fallbackPublic = PublicData(
                standardProcessingRange = publicDataDoc.standardProcessing.rangeLabel,
                standardProcessingMinMonths = publicDataDoc.standardProcessing.minMonths,
                standardProcessingMaxMonths = publicDataDoc.standardProcessing.maxMonths,
                regionalNote = "",
            )
            return predict(profile, fallbackPublic, today)
                ?.copy(confidenceLevel = ConfidenceLevel.LOW)
        }

        val submitMonth = DateLabels.formatMonth(submitted)
        val wait = computeWait(series, submitMonth, today)

        val reasons = mutableListOf<String>()
        val officeData = publicDataDoc.officeData(office!!.name)
        reasons += "基于${officeData?.displayName ?: context.getString(office.labelRes)}真实受理处理数据（更新至 ${publicDataDoc.dataAsOf}）"

        // Calibrate the raw FIFO wait to real observed latency (see OfficeData.calibrationFactor).
        // Applied to time only — rNow (people ahead) stays a real count.
        val factor = officeData?.calibrationFactor ?: 1.0
        val optimisticMonths = wait.optimisticMonths * factor
        val normalMonths = wait.normalMonths * factor
        val conservativeMonths = wait.conservativeMonths * factor

        // Add fractional months as days so the three scenarios land on distinct dates.
        // Truncating to whole months (toLong) collapsed e.g. 9.2 and 9.9 into the same
        // month, making 正常 and 保守 show identical labels.
        val optimisticDate = today.plusFractionalMonths(optimisticMonths)
        val normalDate = today.plusFractionalMonths(normalMonths)
        val conservativeDate = today.plusFractionalMonths(conservativeMonths)

        val optimisticRange: String
        val normalRange: String
        val conservativeRange: String
        if (wait.state == WaitState.ALREADY_DUE) {
            val dueLabel = "已进入可能出结果区间"
            optimisticRange = dueLabel
            normalRange = dueLabel
            conservativeRange = dueLabel
            reasons += "按统计你已进入可能出结果的区间，请留意入管通知"
        } else {
            optimisticRange = DateLabels.monthThird(optimisticDate)
            normalRange = DateLabels.monthThird(normalDate)
            conservativeRange = DateLabels.monthThird(conservativeDate)
            reasons += "估算你前面约还有 ${wait.rNow} 件待处理"
            reasons += "按近期处理速度推算，约还需 ${normalMonths.roundToInt()} 个月"
        }

        if (profile.submittedDatePrecision == DatePrecision.MONTH) {
            reasons += "申请日期仅精确到月，预测以该月中旬估算，置信度相应降低"
        }
        reasons += "此为按平均处理速度的参考估算，非你个案的实际进度"

        val confidence = fifoConfidence(
            precision = profile.submittedDatePrecision,
            dataAsOf = publicDataDoc.dataAsOf,
            rNow = wait.rNow,
            today = today,
        )

        val waitDays = ChronoUnit.DAYS.between(submitted, today).toInt().coerceAtLeast(0)
        val remainingDays = (normalMonths.coerceAtLeast(0.0) * AVG_DAYS_PER_MONTH).toInt()
        val progress = when {
            wait.state == WaitState.ALREADY_DUE -> 99
            waitDays + remainingDays <= 0 -> 0
            else -> min(99, waitDays * 100 / (waitDays + remainingDays))
        }

        return Prediction(
            applicationId = profile.id,
            optimisticRange = optimisticRange,
            normalRange = normalRange,
            conservativeRange = conservativeRange,
            confidenceLevel = confidence,
            currentWaitDays = waitDays,
            progressPercent = progress,
            reasons = reasons,
        )
    }

    /** Confidence for the FIFO path: starts HIGH, docked for imprecision/staleness. */
    private fun fifoConfidence(
        precision: DatePrecision,
        dataAsOf: String,
        rNow: Int,
        today: LocalDate,
    ): ConfidenceLevel {
        var points = 2
        if (precision == DatePrecision.MONTH) points -= 1
        if (dataAgeMonths(dataAsOf, today) > 12) points -= 1
        if (rNow <= 0) points -= 1
        return when {
            points >= 2 -> ConfidenceLevel.HIGH
            points == 1 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }

    /** Adds [months] (may be fractional) to a date by converting to whole days. */
    private fun LocalDate.plusFractionalMonths(months: Double): LocalDate =
        plusDays((months.coerceAtLeast(0.0) * AVG_DAYS_PER_MONTH).roundToInt().toLong())

    private fun dataAgeMonths(dataAsOf: String, today: LocalDate): Int {
        val parts = dataAsOf.split("-")
        if (parts.size != 2) return 0
        val year = parts[0].toIntOrNull() ?: return 0
        val month = parts[1].toIntOrNull() ?: return 0
        return (today.year - year) * 12 + (today.monthValue - month)
    }

    private fun officeBuffer(office: ImmigrationOffice?): Int = when (office) {
        ImmigrationOffice.TOKYO, ImmigrationOffice.YOKOHAMA -> 1
        else -> 0
    }

    private companion object {
        const val AVG_DAYS_PER_MONTH = 30.44
    }

    private fun confidence(
        precision: DatePrecision,
        hasSupplement: Boolean,
        office: ImmigrationOffice?,
    ): ConfidenceLevel {
        var points = 2
        if (precision == DatePrecision.MONTH) points -= 1
        if (hasSupplement) points -= 1
        if (office == null || office == ImmigrationOffice.OTHER) points -= 1
        return when {
            points >= 2 -> ConfidenceLevel.HIGH
            points == 1 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }
}
