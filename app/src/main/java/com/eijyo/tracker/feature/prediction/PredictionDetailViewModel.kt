package com.eijyo.tracker.feature.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.staticdata.PublicDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Read-only view-state for the Prediction Detail page (PM doc §9). */
data class PredictionDetailUiState(
    val available: Boolean = false,
    val normalRange: String = "",
    val progressPercent: Int = 0,
    val confidenceLabel: String = "",
    val office: String = "",
    val optimisticRange: String = "",
    val conservativeRange: String = "",
    val processingRange: String = "",
    val waitDays: Int = 0,
    val pathLabel: String = "",
    val supplementStatus: String = "",
    val sourceName: String = "",
    val sourceUpdated: String = "",
)

@HiltViewModel
class PredictionDetailViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    analysisRepository: AnalysisRepository,
) : ViewModel() {

    val state: StateFlow<PredictionDetailUiState> = combine(
        profileRepository.observeApplication(),
        analysisRepository.observePrediction(),
    ) { app, prediction ->
        if (app == null || prediction == null) {
            PredictionDetailUiState(available = false)
        } else {
            val pub = PublicDataSource.forOffice(app.submittedOffice)
            PredictionDetailUiState(
                available = true,
                normalRange = prediction.normalRange,
                progressPercent = prediction.progressPercent,
                confidenceLabel = prediction.confidenceLevel.label,
                office = app.submittedOffice?.label ?: "未指定入管",
                optimisticRange = prediction.optimisticRange,
                conservativeRange = prediction.conservativeRange,
                processingRange = pub.standardProcessingRange,
                waitDays = prediction.currentWaitDays,
                pathLabel = pathLabel(app.visaType, app.applicationPath),
                supplementStatus = if (app.hasSupplementRequest == TriState.YES) "已收到" else "未发生",
                sourceName = pub.sourceName,
                sourceUpdated = pub.sourceUpdatedAt,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PredictionDetailUiState(),
    )
}

private fun pathLabel(visa: VisaType?, path: ApplicationPath?): String =
    listOfNotNull(visa?.let(::shortVisa), path?.label).joinToString(" · ").ifBlank { "未指定" }

/** Compact visa label so the factor row fits on one line (full labels are long). */
private fun shortVisa(visa: VisaType): String = when (visa) {
    VisaType.ENGINEER -> "技人国"
    VisaType.HIGHLY_SKILLED -> "高度专门职"
    VisaType.SPOUSE_OF_JAPANESE -> "日本人配偶"
    VisaType.SPOUSE_OF_PR -> "永住者配偶"
    VisaType.LONG_TERM_RESIDENT -> "定住者"
    VisaType.DEPENDENT -> "家族滞在"
}
