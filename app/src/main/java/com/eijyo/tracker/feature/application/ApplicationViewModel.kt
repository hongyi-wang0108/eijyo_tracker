package com.eijyo.tracker.feature.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.ResultType
import com.eijyo.tracker.data.model.SupplementRequest
import com.eijyo.tracker.data.model.SupplementStatus
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.SupplementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class TimelineDot { MINT, SKY, LAVENDER, CORAL }

data class TimelineDisplayItem(
    val dot: TimelineDot,
    val dateLabel: String,
    val title: String,
    val subtitle: String,
    val isPending: Boolean = false,
)

data class ApplicationUiState(
    val officeName: String = "",
    val statusLabel: String = "",
    val waitDaysLabel: String = "",
    val stagePillLabel: String = "",
    val visaTypeLabel: String = "",
    val pathLabel: String = "",
    val submittedDateDisplay: String = "",
    val timeline: List<TimelineDisplayItem> = emptyList(),
    val hasPendingSupplement: Boolean = false,
)

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val supplementRepo: SupplementRepository,
    private val analysisRepo: AnalysisRepository,
) : ViewModel() {

    val state = combine(
        profileRepo.observeApplication(),
        supplementRepo.observeByApplication(),
        analysisRepo.observePrediction(),
    ) { profile, supplements, prediction ->
        buildState(profile, supplements, prediction)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApplicationUiState())

    private fun buildState(
        profile: ApplicationProfile?,
        supplements: List<SupplementRequest>,
        prediction: Prediction?,
    ): ApplicationUiState {
        if (profile == null) return ApplicationUiState()
        val waitDays = calcWaitDays(profile.submittedDate)
        return ApplicationUiState(
            officeName = profile.submittedOffice?.label ?: "",
            statusLabel = profile.status.label,
            waitDaysLabel = if (waitDays != null) "已提交 $waitDays 天" else "",
            stagePillLabel = stagePill(profile.status, profile.resultType),
            visaTypeLabel = profile.visaType?.label ?: "",
            pathLabel = profile.applicationPath?.label ?: "",
            submittedDateDisplay = profile.submittedDate?.replace("-", ".") ?: "",
            timeline = buildTimeline(profile, supplements, prediction),
            hasPendingSupplement = supplements.any { it.status == SupplementStatus.RECEIVED },
        )
    }

    private fun stagePill(status: ApplicationStatus, result: ResultType): String = when (status) {
        ApplicationStatus.PREPARING -> "准备提交"
        ApplicationStatus.REVIEWING -> "等待结果中"
        ApplicationStatus.COMPLETED -> when (result) {
            ResultType.APPROVED -> "已许可"
            ResultType.REJECTED -> "未许可"
            ResultType.WITHDRAWN -> "已撤回"
            ResultType.UNKNOWN -> "已结束"
        }
    }

    private fun buildTimeline(
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

    private fun shortRange(normalRange: String): String {
        val year = Regex("(\\d{4})年").find(normalRange)?.groupValues?.get(1) ?: return "预计"
        val months = Regex("(\\d{1,2})月").findAll(normalRange).map { it.groupValues[1] }.toList()
        return if (months.size >= 2) "预计 $year.${months[0].padStart(2, '0')} - ${months.last()}"
        else if (months.size == 1) "预计 $year.${months[0].padStart(2, '0')}"
        else "预计 $year"
    }

    private fun calcWaitDays(submittedDate: String?): Int? {
        submittedDate ?: return null
        return try {
            val fmt = if (submittedDate.length == 7) SimpleDateFormat("yyyy-MM", Locale.getDefault())
            else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = fmt.parse(submittedDate) ?: return null
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - date.time).toInt()
        } catch (e: Exception) { null }
    }

    // --- Event recording ---

    fun addSupplementReceived(receivedDate: String, deadline: String, description: String) {
        viewModelScope.launch {
            supplementRepo.save(
                SupplementRequest(
                    receivedDate = receivedDate.ifBlank { null },
                    deadlineDate = deadline.ifBlank { null },
                    type = description,
                    status = SupplementStatus.RECEIVED,
                )
            )
            val profile = profileRepo.getApplication() ?: return@launch
            val updated = profile.copy(hasSupplementRequest = TriState.YES)
            profileRepo.saveApplication(updated)
            analysisRepo.regenerate(updated)
        }
    }

    fun addSupplementSubmitted(submittedDate: String) {
        viewModelScope.launch {
            val sups = supplementRepo.observeByApplication()
                .map { it.filter { s -> s.status == SupplementStatus.RECEIVED } }
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .value
            val pending = sups.firstOrNull() ?: return@launch
            supplementRepo.save(
                pending.copy(
                    status = SupplementStatus.SUBMITTED,
                    submittedDate = submittedDate.ifBlank { null },
                )
            )
        }
    }

    fun recordResult(type: ResultType, resultDate: String) {
        viewModelScope.launch {
            val profile = profileRepo.getApplication() ?: return@launch
            val updated = profile.copy(
                status = ApplicationStatus.COMPLETED,
                resultType = type,
                resultDate = resultDate.ifBlank { null },
            )
            profileRepo.saveApplication(updated)
            analysisRepo.regenerate(updated)
        }
    }
}
