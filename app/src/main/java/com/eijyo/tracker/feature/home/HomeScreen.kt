package com.eijyo.tracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.CardLabel
import com.eijyo.tracker.core.ui.component.DogMascot
import com.eijyo.tracker.core.ui.component.MacaronCard
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.RiskLevel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EijyoTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = state.greeting,
            style = EijyoTheme.typography.headlineMedium,
            color = colors.ink,
        )
        StatusSummaryRow(summary = state.statusSummary, waitDays = state.waitDays)

        DogAssistantCard(status = state.status)
        PredictionCard(state = state)
        RiskCard(level = state.riskLevel)
        DocumentsCard(prepared = state.documentsPrepared, total = state.documentsTotal)
    }
}

@Composable
private fun StatusSummaryRow(summary: String, waitDays: Int?) {
    val colors = EijyoTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.mintContainer)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(summary, style = EijyoTheme.typography.labelMedium, color = colors.ink)
        }
        if (waitDays != null) {
            Spacer(Modifier.width(12.dp))
            Text("已提交 $waitDays 天", style = EijyoTheme.typography.bodyMedium, color = colors.inkMuted)
        }
    }
}

@Composable
private fun DogAssistantCard(status: ApplicationStatus) {
    val colors = EijyoTheme.colors
    val message = when (status) {
        ApplicationStatus.PREPARING -> "我们一步步来，先把材料准备齐，别着急～"
        ApplicationStatus.REVIEWING -> "已经交上去啦，安心等结果，我陪着你。"
        ApplicationStatus.COMPLETED -> "辛苦啦！结果已经记录好了。"
    }
    MacaronCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DogMascot(size = 64.dp)
            Spacer(Modifier.width(16.dp))
            Text(
                text = message,
                style = EijyoTheme.typography.bodyLarge,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PredictionCard(state: HomeUiState) {
    val colors = EijyoTheme.colors
    MacaronCard(modifier = Modifier.fillMaxWidth()) {
        CardLabel("预计结果区间")
        val range = state.predictionRange
        if (state.status == ApplicationStatus.REVIEWING && range != null) {
            Text(
                text = range,
                style = EijyoTheme.typography.titleLarge,
                color = colors.ink,
                modifier = Modifier.padding(top = 6.dp),
            )
            state.confidenceLabel?.let {
                Text(
                    text = "置信度：$it",
                    style = EijyoTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            state.progressPercent?.let { pct ->
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = colors.mint,
                    trackColor = colors.mintContainer,
                )
                Text(
                    text = "审查进度约 $pct%",
                    style = EijyoTheme.typography.labelMedium,
                    color = colors.inkMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        } else {
            Text(
                text = state.predictionPlaceholder ?: "暂无预测",
                style = EijyoTheme.typography.bodyLarge,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(
            text = "预测仅供参考，不构成法律意见。",
            style = EijyoTheme.typography.labelSmall,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun RiskCard(level: RiskLevel?) {
    val colors = EijyoTheme.colors
    val (tint, text) = when (level) {
        RiskLevel.LOW -> colors.mintContainer to "未发现明显风险项，保持就好。"
        RiskLevel.MEDIUM -> colors.lemonSoft to "有几项需要确认，建议尽快补强。"
        RiskLevel.HIGH -> colors.peach to "存在较高风险项，建议优先处理。"
        null -> colors.card to "完成问卷后生成风险自检。"
    }
    MacaronCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(level?.label ?: "风险自检", style = EijyoTheme.typography.labelMedium, color = colors.ink)
            }
            Spacer(Modifier.width(12.dp))
            Text(text, style = EijyoTheme.typography.bodyMedium, color = colors.inkMuted, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DocumentsCard(prepared: Int, total: Int) {
    val colors = EijyoTheme.colors
    MacaronCard(modifier = Modifier.fillMaxWidth()) {
        CardLabel("材料完整度")
        Text(
            text = if (total == 0) "尚未生成材料" else "$prepared / $total",
            style = EijyoTheme.typography.titleLarge,
            color = colors.ink,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (total > 0) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { prepared.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = colors.mint,
                trackColor = colors.mintContainer,
            )
        }
    }
}
