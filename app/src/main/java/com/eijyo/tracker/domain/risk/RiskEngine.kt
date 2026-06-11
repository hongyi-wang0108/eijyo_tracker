package com.eijyo.tracker.domain.risk

import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.model.TriState
import javax.inject.Inject

/**
 * Rule-based risk self-check, implementing the additive scoring from PM doc 6.3.4.
 * Pure and explainable: every point added comes with a human-readable factor, and
 * targeted suggestions are returned so the risk screen can show "how to improve".
 */
class RiskEngine @Inject constructor() {

    fun assess(profile: ApplicationProfile): RiskAssessment {
        var score = 0
        val factors = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        score += scorePayment(
            status = profile.taxPaidStatus,
            label = "住民税",
            factors = factors,
            suggestions = suggestions,
        )
        score += scorePayment(
            status = profile.pensionPaidStatus,
            label = "年金",
            factors = factors,
            suggestions = suggestions,
        )
        score += scorePayment(
            status = profile.healthInsuranceStatus,
            label = "健康保险",
            factors = factors,
            suggestions = suggestions,
        )

        if (isLowIncome(profile.annualIncomeRange) && profile.dependentsCount >= 2) {
            score += 2
            factors += "年收偏低且扶养人数较多，生计稳定性可能受质疑"
            suggestions += "准备能体现稳定收入与资产的材料，必要时补充存款或在职证明"
        }

        if (profile.applicationPath == ApplicationPath.HSP_70 ||
            profile.applicationPath == ApplicationPath.HSP_80
        ) {
            score += 1
            factors += "高度人才路径，分数佐证材料尚未确认完整"
            suggestions += "完成高度人材积分计算表并准备对应疎明资料"
        }

        if (profile.hasSupplementRequest == TriState.YES) {
            score += 1
            factors += "存在补资料请求，需确认是否已按期提交"
            suggestions += "尽快在期限内提交补资料，并保留提交凭证"
        }

        if (factors.isEmpty()) {
            factors += "未发现明显风险项，材料状态良好"
        }

        return RiskAssessment(
            applicationId = profile.id,
            level = toLevel(score),
            score = score,
            factors = factors,
            suggestions = suggestions,
        )
    }

    private fun scorePayment(
        status: TriState,
        label: String,
        factors: MutableList<String>,
        suggestions: MutableList<String>,
    ): Int = when (status) {
        TriState.YES -> 0
        TriState.UNKNOWN -> {
            factors += "$label 缴纳情况不确定"
            suggestions += "确认 $label 的缴纳记录，并准备对应证明"
            1
        }
        TriState.NO -> {
            factors += "$label 存在未按时缴纳"
            suggestions += "补缴 $label 并保留缴纳凭证，说明情况"
            2
        }
    }

    private fun isLowIncome(income: IncomeRange?): Boolean =
        income == IncomeRange.BELOW_300 || income == IncomeRange.R300_400

    private fun toLevel(score: Int): RiskLevel = when {
        score <= 1 -> RiskLevel.LOW
        score <= 3 -> RiskLevel.MEDIUM
        else -> RiskLevel.HIGH
    }
}
