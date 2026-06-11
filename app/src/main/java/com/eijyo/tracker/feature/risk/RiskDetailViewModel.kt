package com.eijyo.tracker.feature.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    val modelType: String = "规则模型",
)

@HiltViewModel
class RiskDetailViewModel @Inject constructor(
    private val analysisRepo: AnalysisRepository,
) : ViewModel() {

    val state = analysisRepo.observeRisk()
        .map { risk -> buildState(risk) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RiskDetailUiState())

    private fun buildState(risk: RiskAssessment?): RiskDetailUiState {
        if (risk == null) return RiskDetailUiState()
        return RiskDetailUiState(
            available = true,
            levelLabel = risk.level.label,
            riskLevel = risk.level,
            displayScore = displayScore(risk.level, risk.score),
            summary = summaryFor(risk),
            updatedLabel = relativeTime(risk.createdAt),
            sections = deriveSections(risk),
            recommendations = risk.suggestions,
            recommendationNote = "这会影响材料完整度和预测置信度。",
            modelType = "规则模型",
        )
    }

    private fun displayScore(level: RiskLevel, rawScore: Int): Int = when (level) {
        RiskLevel.LOW -> (100 - rawScore * 8).coerceIn(80, 100)
        RiskLevel.MEDIUM -> (75 - rawScore * 8).coerceIn(50, 74)
        RiskLevel.HIGH -> (45 - rawScore * 5).coerceIn(15, 49)
    }

    private fun summaryFor(risk: RiskAssessment): String = when (risk.level) {
        RiskLevel.LOW -> "纳税、年金、保险状态均已确认"
        RiskLevel.MEDIUM -> "存在部分待确认项，请检查下方建议"
        RiskLevel.HIGH -> "存在明显风险项，建议尽快确认"
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

    private fun deriveSections(risk: RiskAssessment): List<RiskSectionItem> {
        val factors = risk.factors
        val publicIssue = factors.any { "住民税" in it || "年金" in it || "健康保险" in it }
        val livelihoodIssue = factors.any { "年收" in it || "生计" in it }
        val conductIssue = factors.any { "补资料" in it || "高度人才" in it }
        return listOf(
            RiskSectionItem(
                title = "素行",
                subtitle = "交通/入管记录待持续确认",
                statusLabel = if (conductIssue) "需确认" else "无明显问题",
                status = if (conductIssue) SectionStatus.WARN else SectionStatus.OK,
            ),
            RiskSectionItem(
                title = "生计",
                subtitle = if (livelihoodIssue) "年收或扶养人数存在关注项" else "年收与扶养人数匹配",
                statusLabel = if (livelihoodIssue) "有待确认" else "稳定",
                status = if (livelihoodIssue) SectionStatus.WARN else SectionStatus.INFO,
            ),
            RiskSectionItem(
                title = "公益性",
                subtitle = if (publicIssue) "纳税・年金・保险存在关注项" else "纳税・年金・保险正常",
                statusLabel = if (publicIssue) "需确认" else "已确认",
                status = if (publicIssue) SectionStatus.WARN else SectionStatus.CHECKED,
            ),
        )
    }
}
