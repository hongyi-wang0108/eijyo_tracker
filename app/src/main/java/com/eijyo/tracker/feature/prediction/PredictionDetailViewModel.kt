package com.eijyo.tracker.feature.prediction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.R
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.PublicDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    @ApplicationContext private val context: Context,
    profileRepository: ProfileRepository,
    analysisRepository: AnalysisRepository,
    private val publicDataRepository: PublicDataRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { runCatching { publicDataRepository.load() } }
    }

    val state: StateFlow<PredictionDetailUiState> = combine(
        profileRepository.observeApplication(),
        analysisRepository.observePrediction(),
        publicDataRepository.state,
    ) { app, prediction, dataResult ->
        if (app == null || prediction == null) {
            PredictionDetailUiState(available = false)
        } else {
            val doc = dataResult?.doc
            PredictionDetailUiState(
                available = true,
                normalRange = prediction.normalRange,
                progressPercent = prediction.progressPercent,
                confidenceLabel = context.getString(prediction.confidenceLevel.labelRes),
                office = app.submittedOffice?.let { context.getString(it.labelRes) }
                    ?: context.getString(R.string.pred_office_unspecified),
                optimisticRange = prediction.optimisticRange,
                conservativeRange = prediction.conservativeRange,
                processingRange = doc?.standardProcessing?.rangeLabel ?: "4 - 6 个月",
                waitDays = prediction.currentWaitDays,
                pathLabel = pathLabel(app.visaType, app.applicationPath),
                supplementStatus = if (app.hasSupplementRequest == TriState.YES)
                    context.getString(R.string.pred_supplement_received_status)
                else
                    context.getString(R.string.pred_supplement_not_occurred),
                sourceName = doc?.source?.name ?: "出入国在留管理庁",
                sourceUpdated = doc?.dataAsOf?.let {
                    context.getString(R.string.pred_source_updated_fmt, monthLabel(it))
                } ?: "",
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PredictionDetailUiState(),
    )

    /** "2026-03" → locale-formatted year-month; passthrough if unparseable. */
    private fun monthLabel(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return context.getString(R.string.date_year_month_fmt, parts[0], month.toString())
    }

    private fun pathLabel(visa: VisaType?, path: ApplicationPath?): String =
        listOfNotNull(visa?.let { shortVisa(it) }, path?.let { context.getString(it.labelRes) })
            .joinToString(" · ")
            .ifBlank { context.getString(R.string.pred_path_unspecified) }

    /** Compact visa label so the factor row fits on one line (full labels are long). */
    private fun shortVisa(visa: VisaType): String = when (visa) {
        VisaType.ENGINEER -> context.getString(R.string.visa_abbr_engineer)
        VisaType.HIGHLY_SKILLED -> context.getString(R.string.visa_abbr_highly_skilled)
        VisaType.SPOUSE_OF_JAPANESE -> context.getString(R.string.visa_abbr_spouse_of_japanese)
        VisaType.SPOUSE_OF_PR -> context.getString(R.string.visa_abbr_spouse_of_pr)
        VisaType.LONG_TERM_RESIDENT -> context.getString(R.string.visa_abbr_long_term_resident)
        VisaType.DEPENDENT -> context.getString(R.string.visa_abbr_dependent)
    }
}
