package com.eijyo.tracker.domain.timeline

import android.content.Context
import com.eijyo.tracker.R
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.ResultType
import com.eijyo.tracker.data.model.SupplementRequest
import com.eijyo.tracker.data.model.SupplementStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Color role for a timeline node, mapped to theme colors at the UI layer. */
enum class TimelineDot { MINT, SKY, LAVENDER, CORAL }

data class TimelineDisplayItem(
    val dot: TimelineDot,
    val dateLabel: String,
    val title: String,
    val subtitle: String,
    val isPending: Boolean = false,
)

/**
 * Builds the application timeline from the profile, supplement events and prediction.
 * Shared by the Application tab (full, editable) and the Home dashboard (read-only
 * summary via [summary]) so both stay consistent — a single source of truth for the
 * "提交 → 受理 → 补资料 → 结果" narrative.
 */
class TimelineBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun build(
        profile: ApplicationProfile,
        supplements: List<SupplementRequest>,
        prediction: Prediction?,
    ): List<TimelineDisplayItem> {
        val items = mutableListOf<TimelineDisplayItem>()
        val subDate = profile.submittedDate?.replace("-", ".")

        items += TimelineDisplayItem(
            dot = TimelineDot.MINT,
            dateLabel = subDate ?: context.getString(R.string.timeline_date_pending),
            title = context.getString(R.string.timeline_submitted_title),
            subtitle = context.getString(R.string.timeline_submitted_subtitle),
            isPending = subDate == null,
        )

        if (subDate != null) {
            items += TimelineDisplayItem(
                dot = TimelineDot.SKY,
                dateLabel = subDate,
                title = context.getString(R.string.timeline_accepted_title),
                subtitle = context.getString(R.string.timeline_accepted_subtitle),
            )
        }

        supplements.sortedBy { it.receivedDate }.forEach { sup ->
            items += TimelineDisplayItem(
                dot = TimelineDot.CORAL,
                dateLabel = sup.receivedDate?.replace("-", ".") ?: context.getString(R.string.timeline_date_unknown),
                title = context.getString(R.string.timeline_supplement_received_title),
                subtitle = sup.type.ifEmpty { context.getString(R.string.timeline_supplement_received_subtitle_empty) },
            )
            if (sup.status == SupplementStatus.SUBMITTED) {
                items += TimelineDisplayItem(
                    dot = TimelineDot.MINT,
                    dateLabel = sup.submittedDate?.replace("-", ".") ?: context.getString(R.string.timeline_date_unknown),
                    title = context.getString(R.string.timeline_supplement_submitted_title),
                    subtitle = context.getString(R.string.timeline_supplement_submitted_subtitle),
                )
            }
        }

        if (profile.status == ApplicationStatus.REVIEWING) {
            val predLabel = prediction?.normalRange?.let { shortRange(it) }
                ?: context.getString(R.string.timeline_range_prefix)
            items += TimelineDisplayItem(
                dot = TimelineDot.LAVENDER,
                dateLabel = predLabel,
                title = context.getString(R.string.timeline_result_title),
                subtitle = if (prediction != null)
                    context.getString(R.string.timeline_result_subtitle_estimated)
                else
                    context.getString(R.string.timeline_result_subtitle_waiting),
                isPending = prediction == null,
            )
            items += TimelineDisplayItem(
                dot = TimelineDot.CORAL,
                dateLabel = context.getString(R.string.timeline_date_future),
                title = context.getString(R.string.timeline_notice_title),
                subtitle = context.getString(R.string.timeline_notice_subtitle),
                isPending = true,
            )
        }

        if (profile.status == ApplicationStatus.COMPLETED && profile.resultType != ResultType.UNKNOWN) {
            items += TimelineDisplayItem(
                dot = if (profile.resultType == ResultType.APPROVED) TimelineDot.MINT else TimelineDot.CORAL,
                dateLabel = profile.resultDate?.replace("-", ".") ?: context.getString(R.string.timeline_date_unknown),
                title = context.getString(profile.resultType.labelRes),
                subtitle = when (profile.resultType) {
                    ResultType.APPROVED -> context.getString(R.string.timeline_result_approved_subtitle)
                    ResultType.REJECTED -> context.getString(R.string.timeline_result_rejected_subtitle)
                    ResultType.WITHDRAWN -> context.getString(R.string.timeline_result_withdrawn_subtitle)
                    else -> ""
                },
            )
        }

        return items
    }

    /**
     * Read-only summary for the Home dashboard: the same nodes, capped to [limit] so the
     * card stays compact. Keeps the first submit/受理 nodes and the most relevant tail
     * (latest supplement + outcome), preserving chronological meaning.
     */
    fun summary(
        profile: ApplicationProfile,
        supplements: List<SupplementRequest>,
        prediction: Prediction?,
        limit: Int = 4,
    ): List<TimelineDisplayItem> {
        val full = build(profile, supplements, prediction)
        if (full.size <= limit) return full
        val head = full.take(2)
        val tail = full.takeLast(limit - 2)
        return head + tail
    }

    private fun shortRange(normalRange: String): String {
        val prefix = context.getString(R.string.timeline_range_prefix)
        val year = Regex("(\\d{4})年").find(normalRange)?.groupValues?.get(1) ?: return prefix
        val months = Regex("(\\d{1,2})月").findAll(normalRange).map { it.groupValues[1] }.toList()
        return if (months.size >= 2) "$prefix $year.${months[0].padStart(2, '0')} - ${months.last()}"
        else if (months.size == 1) "$prefix $year.${months[0].padStart(2, '0')}"
        else "$prefix $year"
    }
}
