package com.eijyo.tracker.feature.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.model.TriState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val modelType: String = "规则模型",
)

@HiltViewModel
class RiskDetailViewModel @Inject constructor(
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
            levelLabel = risk.level.label,
            riskLevel = risk.level,
            displayScore = displayScore(risk.level, risk.score),
            summary = summaryFor(risk, profile),
            updatedLabel = relativeTime(risk.createdAt),
            sections = deriveSections(risk, profile),
            recommendations = risk.suggestions,
            recommendationNote = recommendationNote(risk),
            sourceSummary = sourceSummary(profile),
            modelType = "规则模型",
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
        RiskLevel.LOW -> "当前问卷里没有明显风险项，建议继续保持材料和缴纳记录完整"
        RiskLevel.MEDIUM -> "问卷里存在不确定或待确认项，请先处理下方提示"
        RiskLevel.HIGH -> "当前档案里有明确风险项，建议优先补强再继续推进"
    }

    private fun relativeTime(createdAt: Long): String {
        val diffMs = System.currentTimeMillis() - createdAt
        return when {
            diffMs < TimeUnit.MINUTES.toMillis(2) -> "刚刚更新"
            diffMs < TimeUnit.HOURS.toMillis(1) -> "今天更新"
            diffMs < TimeUnit.DAYS.toMillis(1) -> "今天更新"
            else -> "最近更新"
        }
    }

    private fun deriveSections(risk: RiskAssessment, profile: ApplicationProfile?): List<RiskSectionItem> {
        val publicIssue = publicInterestIssue(profile)
        val livelihoodIssue = livelihoodIssue(profile)
        val conductIssue = conductIssue(profile)
        return listOf(
            RiskSectionItem(
                title = "素行",
                subtitle = when {
                    profile == null -> "暂无申请档案，无法判断"
                    profile.hasSupplementRequest == TriState.YES -> "你填过补资料记录，建议确认是否已按期提交并留存凭证"
                    else -> "当前问卷没有违法或违章字段，这里只按补资料状态做提醒，其他需你自行确认"
                },
                statusLabel = when {
                    profile == null -> "暂无数据"
                    conductIssue -> "持续确认"
                    else -> "自行确认"
                },
                status = when {
                    profile == null -> SectionStatus.INFO
                    conductIssue -> SectionStatus.WARN
                    else -> SectionStatus.INFO
                },
            ),
            RiskSectionItem(
                title = "生计",
                subtitle = when {
                    profile == null -> "暂无申请档案，无法判断"
                    livelihoodIssue -> "年收或扶养人数会影响稳定性判断"
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> "你没有填写年收，生计稳定性会更保守"
                    else -> "当前年收与扶养人数没有触发明显风险规则"
                },
                statusLabel = when {
                    profile == null -> "暂无数据"
                    livelihoodIssue -> "有待确认"
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> "待补充"
                    else -> "稳定"
                },
                status = when {
                    profile == null -> SectionStatus.INFO
                    livelihoodIssue -> SectionStatus.WARN
                    profile.annualIncomeRange == IncomeRange.UNDISCLOSED -> SectionStatus.INFO
                    else -> SectionStatus.INFO
                },
            ),
            RiskSectionItem(
                title = "公益性",
                subtitle = when {
                    profile == null -> "暂无申请档案，无法判断"
                    publicIssue -> "纳税・年金・保险里至少有一项未确认或异常"
                    else -> "纳税・年金・保险三项都按你问卷里的答案通过了基础检查"
                },
                statusLabel = when {
                    profile == null -> "暂无数据"
                    publicIssue -> "需确认"
                    else -> "已确认"
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
        if (risk.suggestions.isEmpty()) "" else "这些建议直接来自你问卷里被判成未确认、未缴、补资料或生计偏弱的项目。"

    private fun sourceSummary(profile: ApplicationProfile?): String {
        if (profile == null) return "当前没有可用申请档案，所以这里只能显示空态。"
        val pathLabel = when (profile.applicationPath) {
            ApplicationPath.HSP_70, ApplicationPath.HSP_80 -> "高度人才路径"
            null -> "未填写申请路径"
            else -> profile.applicationPath.label
        }
        return "基于你在问卷和申请档案里填写的纳税、年金、健康保险、年收、扶养人数、申请路径和补资料状态生成。当前路径：$pathLabel。"
    }
}
