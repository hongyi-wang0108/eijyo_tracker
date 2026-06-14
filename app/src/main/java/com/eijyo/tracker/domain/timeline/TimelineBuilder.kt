package com.eijyo.tracker.domain.timeline

import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.ResultType
import com.eijyo.tracker.data.model.SupplementRequest
import com.eijyo.tracker.data.model.SupplementStatus
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
class TimelineBuilder @Inject constructor() {

    fun build(
        profile: ApplicationProfile,
        supplements: List<SupplementRequest>,
        prediction: Prediction?,
    ): List<TimelineDisplayItem> {
        val items = mutableListOf<TimelineDisplayItem>()
        val subDate = profile.submittedDate?.replace("-", ".")

        // 已提交申请
        items += TimelineDisplayItem(
            dot = TimelineDot.MINT,
            dateLabel = subDate ?: "待填写",
            title = "已提交申请",
            subtitle = "材料已交给入管",
            isPending = subDate == null,
        )

        // 入管受理
        if (subDate != null) {
            items += TimelineDisplayItem(
                dot = TimelineDot.SKY,
                dateLabel = subDate,
                title = "入管受理",
                subtitle = "开始进入审查流程",
            )
        }

        // 补资料事件（按收到日期排序）
        supplements.sortedBy { it.receivedDate }.forEach { sup ->
            items += TimelineDisplayItem(
                dot = TimelineDot.CORAL,
                dateLabel = sup.receivedDate?.replace("-", ".") ?: "未知",
                title = "收到补资料",
                subtitle = sup.type.ifEmpty { "请确认入管要求" },
            )
            if (sup.status == SupplementStatus.SUBMITTED) {
                items += TimelineDisplayItem(
                    dot = TimelineDot.MINT,
                    dateLabel = sup.submittedDate?.replace("-", ".") ?: "未知",
                    title = "补资料已提交",
                    subtitle = "已按期提交给入管",
                )
            }
        }

        // 预计结果（仅审查中时）
        if (profile.status == ApplicationStatus.REVIEWING) {
            val predLabel = prediction?.normalRange?.let { shortRange(it) } ?: "预计"
            items += TimelineDisplayItem(
                dot = TimelineDot.LAVENDER,
                dateLabel = predLabel,
                title = "审查结果",
                subtitle = if (prediction != null) "基于公开数据估算" else "等待预测数据",
                isPending = prediction == null,
            )
            items += TimelineDisplayItem(
                dot = TimelineDot.CORAL,
                dateLabel = "待发生",
                title = "通知书 / 明信片 / 补资料",
                subtitle = "有新状态会提醒你",
                isPending = true,
            )
        }

        // 最终结果（已结束时）
        if (profile.status == ApplicationStatus.COMPLETED && profile.resultType != ResultType.UNKNOWN) {
            items += TimelineDisplayItem(
                dot = if (profile.resultType == ResultType.APPROVED) TimelineDot.MINT else TimelineDot.CORAL,
                dateLabel = profile.resultDate?.replace("-", ".") ?: "未知",
                title = profile.resultType.label,
                subtitle = when (profile.resultType) {
                    ResultType.APPROVED -> "永住许可已获批准"
                    ResultType.REJECTED -> "申请未获批准"
                    ResultType.WITHDRAWN -> "申请已撤回"
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
        // Keep the first two (提交/受理) and the last (limit-2) nodes (latest events/outcome).
        val head = full.take(2)
        val tail = full.takeLast(limit - 2)
        return head + tail
    }

    private fun shortRange(normalRange: String): String {
        val year = Regex("(\\d{4})年").find(normalRange)?.groupValues?.get(1) ?: return "预计"
        val months = Regex("(\\d{1,2})月").findAll(normalRange).map { it.groupValues[1] }.toList()
        return if (months.size >= 2) "预计 $year.${months[0].padStart(2, '0')} - ${months.last()}"
        else if (months.size == 1) "预计 $year.${months[0].padStart(2, '0')}"
        else "预计 $year"
    }
}
