package com.eijyo.tracker.domain.prediction

import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.ConfidenceLevel
import com.eijyo.tracker.data.model.DateRange
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.staticdata.PublicData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min

/**
 * Rule-based, explainable prediction (PM doc §14 + 6.3.5). It produces three result
 * windows plus a confidence level and the reasons behind them — never a single exact
 * date, never a hidden model.
 *
 * Predictions only exist while [ApplicationStatus.REVIEWING]; if the submitted date is
 * unknown ([DatePrecision.UNKNOWN]) no review window can be computed and this returns
 * null so the UI can prompt the user to fill in the date.
 */
class PredictionEngine @Inject constructor() {

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
            if (it > 0) reasons += "${(profile.submittedOffice ?: ImmigrationOffice.OTHER).label}受理量较大，保守区间相应延长"
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

    private fun officeBuffer(office: ImmigrationOffice?): Int = when (office) {
        ImmigrationOffice.TOKYO, ImmigrationOffice.YOKOHAMA -> 1
        else -> 0
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
