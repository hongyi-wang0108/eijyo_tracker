package com.eijyo.tracker.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.DocumentRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompleteUiState(
    val loading: Boolean = true,
    val statusSummary: String = "",
    val riskLevel: RiskLevel? = null,
    val predictionRange: String? = null,
    val predictionPlaceholder: String? = null,
)

@HiltViewModel
class OnboardingCompleteViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    documentRepository: DocumentRepository,
    analysisRepository: AnalysisRepository,
) : ViewModel() {

    val state: StateFlow<CompleteUiState> = combine(
        profileRepository.observeApplication(),
        documentRepository.observe(),
        analysisRepository.observeRisk(),
        analysisRepository.observePrediction(),
    ) { application, documents, risk, prediction ->
        if (application == null) {
            CompleteUiState(loading = true)
        } else {
            CompleteUiState(
                loading = false,
                statusSummary = statusSummary(application),
                riskLevel = risk?.level,
                predictionRange = prediction?.normalRange.takeIf {
                    application.status == ApplicationStatus.REVIEWING
                },
                predictionPlaceholder = placeholderFor(application),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompleteUiState(),
    )

    private fun statusSummary(app: ApplicationProfile): String = when (app.status) {
        ApplicationStatus.PREPARING -> "准备中"
        ApplicationStatus.REVIEWING ->
            app.submittedOffice?.let { "${it.label} · 审查中" } ?: "审查中"
        ApplicationStatus.COMPLETED -> "结果已记录"
    }

    /** Copy shown in place of a prediction range for non-reviewing states (6.3.5). */
    private fun placeholderFor(app: ApplicationProfile): String? = when (app.status) {
        ApplicationStatus.PREPARING -> "准备进度已建立，先完成材料清单和风险确认"
        ApplicationStatus.COMPLETED -> "结果记录已生成"
        ApplicationStatus.REVIEWING ->
            if (app.submittedDate == null) "补充申请日期后，可以生成预计时间" else null
    }
}
