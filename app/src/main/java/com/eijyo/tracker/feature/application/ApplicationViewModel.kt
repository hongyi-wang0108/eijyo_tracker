package com.eijyo.tracker.feature.application

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.R
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
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
            officeName = profile.submittedOffice?.let { context.getString(it.labelRes) } ?: "",
            statusLabel = context.getString(profile.status.labelRes),
            waitDaysLabel = if (waitDays != null) context.getString(R.string.app_wait_days_fmt, waitDays) else "",
            stagePillLabel = stagePill(profile.status, profile.resultType),
            visaTypeLabel = profile.visaType?.let { context.getString(it.labelRes) } ?: "",
            pathLabel = profile.applicationPath?.let { context.getString(it.labelRes) } ?: "",
            submittedDateDisplay = profile.submittedDate?.replace("-", ".") ?: "",
            timeline = timelineBuilder.build(profile, supplements, prediction),
            hasPendingSupplement = supplements.any { it.status == SupplementStatus.RECEIVED },
        )
    }

    private fun stagePill(status: ApplicationStatus, result: ResultType): String = when (status) {
        ApplicationStatus.PREPARING -> context.getString(R.string.app_stage_preparing)
        ApplicationStatus.REVIEWING -> context.getString(R.string.app_stage_reviewing)
        ApplicationStatus.COMPLETED -> when (result) {
            ResultType.APPROVED -> context.getString(R.string.app_stage_approved)
            ResultType.REJECTED -> context.getString(R.string.app_stage_rejected)
            ResultType.WITHDRAWN -> context.getString(R.string.app_stage_withdrawn)
            ResultType.UNKNOWN -> context.getString(R.string.app_stage_completed)
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
