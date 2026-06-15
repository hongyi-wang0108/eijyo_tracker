package com.eijyo.tracker.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.R
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.DocumentRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
        ApplicationStatus.PREPARING -> context.getString(R.string.appstatus_preparing)
        ApplicationStatus.REVIEWING -> app.submittedOffice
            ?.let { context.getString(R.string.home_status_reviewing_fmt, context.getString(it.labelRes)) }
            ?: context.getString(R.string.home_status_reviewing)
        ApplicationStatus.COMPLETED -> context.getString(R.string.home_status_completed)
    }

    /** Copy shown in place of a prediction range for non-reviewing states (6.3.5). */
    private fun placeholderFor(app: ApplicationProfile): String? = when (app.status) {
        ApplicationStatus.PREPARING -> context.getString(R.string.complete_placeholder_preparing)
        ApplicationStatus.COMPLETED -> context.getString(R.string.home_placeholder_completed)
        ApplicationStatus.REVIEWING ->
            if (app.submittedDate == null) context.getString(R.string.complete_placeholder_no_date) else null
    }
}
