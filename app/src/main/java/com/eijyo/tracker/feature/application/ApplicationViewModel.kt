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
import com.eijyo.tracker.domain.timeline.TimelineBuilder
import com.eijyo.tracker.domain.timeline.TimelineDisplayItem
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
    private val timelineBuilder: TimelineBuilder,
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
            timeline = timelineBuilder.build(profile, supplements, prediction),
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
