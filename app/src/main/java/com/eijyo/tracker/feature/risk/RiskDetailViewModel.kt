package com.eijyo.tracker.feature.risk

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.R
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.model.TriState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class SectionStatus { OK, INFO, CHECKED, WARN }

data class RiskSectionItem(
    val title: String,
    val subtitle: String,
    val statusLabel: String,
    val status: SectionStatus,
)

data class RiskDetailUiState(
    val available: Boolean = false,
    val levelLabel: String = "",
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val displayScore: Int = 0,
    val summary: String = "",
    val updatedLabel: String = "",
    val sections: List<RiskSectionItem> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val recommendationNote: String = "",
    val sourceSummary: String = "",
    val modelType: String = "",
)

@HiltViewModel
class RiskDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analysisRepo: AnalysisRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val state = combine(
        analysisRepo.observeRisk(),
        profileRepository.observeApplication(),
    ) { risk, profile -> buildState(risk, profile) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RiskDetailUiState())

    private fun buildState(risk: RiskAssessment?, profile: ApplicationProfile?): RiskDetailUiState {
        if (risk == null) return RiskDetailUiState()
        return RiskDetailUiState(
            available = true,
            levelLabel = context.getString(risk.level.labelRes),
            riskLevel = risk.level,
            displayScore = displayScore(risk.level, risk.score),
            summary = summaryFor(risk, profile),
            updatedLabel = relativeTime(risk.createdAt),
            sections = deriveSections(risk, profile),
            recommendations = risk.suggestions,
            recommendationNote = recommendationNote(risk),
            sourceSummary = sourceSummary(profile),
            modelType = context.getString(R.string.risk_model_type),
        )
    }

    private fun displayScore(level: RiskLevel, rawScore: Int): Int {
        if (rawScore == 0) return 100
        return when (level) {
            RiskLevel.LOW -> (100 - rawScore * 12).coerceIn(80, 99)
            RiskLevel.MEDIUM -> (78 - rawScore * 9).coerceIn(50, 78)
            RiskLevel.HIGH -> (48 - rawScore * 6).coerceIn(15, 48)
        }
    }

    private fun summaryFor(risk: RiskAssessment, profile: ApplicationProfile?): String = when (risk.level) {
        RiskLevel.LOW -> context.getString(R.string.risk_summary_low)
        RiskLevel.MEDIUM -> context.getString(R.string.risk_summary_medium)
        RiskLevel.HIGH -> context.getString(R.string.risk_summary_high)
    }

    private fun relativeTime(createdAt: Long): String {
        val diffMs = System.currentTimeMillis() - createdAt
        return when {
            diffMs < TimeUnit.MINUTES.toMillis(2) -> context.getString(R.string.risk_updated_just_now)
            diffMs < TimeUnit.DAYS.toMillis(1) -> context.getString(R.string.risk_updated_today)
            else -> context.getString(R.string.risk_updated_recently)
        }
    }

    private fun deriveSections(risk: RiskAssessment, profile: ApplicationProfile?): List<RiskSectionItem> {
        val publicIssue = publicInterestIssue(profile)
        val livelihoodIssue = livelihoodIssue(profile)
        val conductIssue = conductIssue(profile)
        return listOf(
            RiskSectionItem(
                title = context.getString(R.string.risk_section_conduct_title),
                subtitle = when {
                    profile == null -> context.getString(R.string.risk_section_no_profile)
                    profile.hasSupplementRequest == TriState.YES -> context.getString(R.string.risk_conduct_supplement_hint)
                    else -> context.getString(R.string.risk_conduct_default_hint)
                },
                statusLabel = when {
                    profile == null -> context.getString(R.string.risk_status_no_data)
                    conductIssue -> context.getString(R.string.risk_status_check_ongoing)
                    else -> context.getString(R.string.risk_status_self_check)
                },
                status = when {
                    profile == null -> SectionStatus.INFO
                    conductIssue -> SectionStatus.WARN
                    else -> SectionStatus.INFO
                },
            ),
            RiskSectionItem(
                title = context.getString(R.string.risk_section_livelihood_title),
                subtitle = when {
                    profile == null -> context.getString(R.string.risk_section_no_profile)
                    livelihoodIssue -> context.getString(R.string.risk_livelihood_income_issue)
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> context.getString(R.string.risk_livelihood_no_income)
                    else -> context.getString(R.string.risk_livelihood_ok)
                },
                statusLabel = when {
                    profile == null -> context.getString(R.string.risk_status_no_data)
                    livelihoodIssue -> context.getString(R.string.risk_status_pending_confirm)
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> context.getString(R.string.risk_status_supplement_needed)
                    else -> context.getString(R.string.risk_status_stable)
                },
                status = when {
                    profile == null -> SectionStatus.INFO
                    livelihoodIssue -> SectionStatus.WARN
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> SectionStatus.INFO
                    else -> SectionStatus.INFO
                },
            ),
            RiskSectionItem(
                title = context.getString(R.string.risk_section_public_title),
                subtitle = when {
                    profile == null -> context.getString(R.string.risk_section_no_profile)
                    publicIssue -> context.getString(R.string.risk_public_issue)
                    else -> context.getString(R.string.risk_public_ok)
                },
                statusLabel = when {
                    profile == null -> context.getString(R.string.risk_status_no_data)
                    publicIssue -> context.getString(R.string.risk_status_confirm_needed)
                    else -> context.getString(R.string.risk_status_confirmed)
                },
                status = when {
                    profile == null -> SectionStatus.INFO
                    publicIssue -> SectionStatus.WARN
                    else -> SectionStatus.CHECKED
                },
            ),
        )
    }

    private fun conductIssue(profile: ApplicationProfile?): Boolean =
        profile?.hasSupplementRequest == TriState.YES

    private fun livelihoodIssue(profile: ApplicationProfile?): Boolean =
        profile != null &&
            (profile.annualIncomeRange == IncomeRange.BELOW_300 ||
                profile.annualIncomeRange == IncomeRange.R300_400) &&
            profile.dependentsCount >= 2

    private fun publicInterestIssue(profile: ApplicationProfile?): Boolean =
        profile != null && listOf(
            profile.taxPaidStatus,
            profile.pensionPaidStatus,
            profile.healthInsuranceStatus,
        ).any { it != TriState.YES }

    private fun recommendationNote(risk: RiskAssessment): String =
        if (risk.suggestions.isEmpty()) "" else context.getString(R.string.risk_recommendation_note)

    private fun sourceSummary(profile: ApplicationProfile?): String {
        if (profile == null) return context.getString(R.string.risk_source_no_profile)
        val pathLabel = when (profile.applicationPath) {
            ApplicationPath.HSP_70, ApplicationPath.HSP_80 -> context.getString(R.string.risk_path_hsp)
            null -> context.getString(R.string.risk_path_not_filled)
            else -> context.getString(profile.applicationPath.labelRes)
        }
        return context.getString(R.string.risk_source_summary_fmt, pathLabel)
    }
}
