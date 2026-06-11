package com.eijyo.tracker.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.DogFaceSmiling
import com.eijyo.tracker.core.ui.component.PawButton
import com.eijyo.tracker.core.ui.component.WelcomeBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette
import com.eijyo.tracker.data.model.RiskLevel

private val ShadowTint = Color(0x1C8C5C3D)

/**
 * Onboarding Complete (PM doc 6.3): a celebratory "档案已生成" stage card, a 1-2-1 grid
 * of summary cards (prediction / materials / risk / status), and a CTA into the home
 * Dashboard. Layout follows the "Onboarding Complete - Generated" frame.
 */
@Composable
fun OnboardingCompleteScreen(
    onEnterHome: () -> Unit,
    viewModel: OnboardingCompleteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EijyoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screen),
    ) {
        WelcomeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                GeneratedStageCard()

                Spacer(Modifier.height(32.dp))
                Text(
                    text = "申请档案已生成",
                    style = EijyoTheme.typography.headlineMedium.copy(fontSize = 29.sp),
                    color = colors.ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "小狗已经帮你整理好预测、材料和风险提醒。",
                    style = EijyoTheme.typography.bodyLarge,
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(28.dp))
                PredictionSummaryCard(
                    value = state.predictionRange ?: state.predictionPlaceholder ?: "待填写提交日期",
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        iconBg = MacaronPalette.LemonSoft,
                        iconMark = "□",
                        iconColor = colors.lemonAccent,
                        title = "材料完整度",
                        value = "0 / ${state.documentCount}",
                        valueColor = colors.inkMuted,
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        iconBg = colors.skySoft,
                        iconMark = "!",
                        iconColor = colors.skyAccent,
                        title = "风险等级",
                        value = state.riskLevel?.label ?: "—",
                        valueColor = riskColor(state.riskLevel),
                    )
                }

                Spacer(Modifier.height(16.dp))
                StatusSummaryCard(value = state.statusSummary.ifBlank { "准备中" })
            }

            PawButton(
                text = "进入首页",
                onClick = onEnterHome,
                enabled = !state.loading,
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun riskColor(level: RiskLevel?): Color {
    val colors = EijyoTheme.colors
    return when (level) {
        RiskLevel.LOW -> colors.mint
        RiskLevel.MEDIUM -> colors.lemonAccent
        RiskLevel.HIGH -> colors.coral
        null -> colors.inkMuted
    }
}

/** The celebratory stage: rounded card with pastel blobs, a smiling dog face, sparkles and a "整理完成" pill. */
@Composable
private fun GeneratedStageCard() {
    val colors = EijyoTheme.colors
    Box(
        modifier = Modifier
            .size(width = 309.dp, height = 250.dp)
            .shadow(14.dp, RoundedCornerShape(44.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(44.dp))
            .background(MacaronPalette.CreamSoft),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawOval(MacaronPalette.MintContainer, Offset(42.dp.toPx(), 30.dp.toPx()), Size(102.dp.toPx(), 102.dp.toPx()))
            drawOval(MacaronPalette.Pink, Offset(171.dp.toPx(), 38.dp.toPx()), Size(82.dp.toPx(), 82.dp.toPx()))
            drawOval(MacaronPalette.SkySoft, Offset(104.dp.toPx(), 137.dp.toPx()), Size(112.dp.toPx(), 72.dp.toPx()))
        }

        DogFaceSmiling(size = 86.dp, modifier = Modifier.offset(x = 110.dp, y = 66.dp))

        Sparkle("·", 28.sp, MacaronPalette.PinkAccent, 70.dp, 42.dp)
        Sparkle("·", 18.sp, MacaronPalette.LavenderAccent, 230.dp, 62.dp)
        Sparkle("·", 24.sp, MacaronPalette.SkyAccent, 234.dp, 172.dp)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.mintWash)
                .padding(horizontal = 18.dp, vertical = 6.dp),
        ) {
            Text(
                text = "整理完成",
                style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = colors.mint,
            )
        }
    }
}

@Composable
private fun Sparkle(
    symbol: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    x: Dp,
    y: Dp,
) {
    Text(
        text = symbol,
        color = color,
        style = EijyoTheme.typography.labelLarge.copy(fontSize = fontSize),
        modifier = Modifier.offset(x = x, y = y),
    )
}

/** Full-width prediction summary: a green ✓ badge plus the predicted window. */
@Composable
private fun PredictionSummaryCard(value: String) {
    val colors = EijyoTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.card)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        IconBadge(size = 40.dp, bg = colors.mintWash, mark = "✓", markColor = colors.mint, markSize = 22.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text("初步预测区间", style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted)
            Spacer(Modifier.height(4.dp))
            Text(value, style = EijyoTheme.typography.labelLarge.copy(fontSize = 18.sp), color = colors.ink)
        }
    }
}

/** A half-width metric card: an icon badge, a title and a value. */
@Composable
private fun MetricCard(
    modifier: Modifier,
    iconBg: Color,
    iconMark: String,
    iconColor: Color,
    title: String,
    value: String,
    valueColor: Color,
) {
    val colors = EijyoTheme.colors
    Column(
        modifier = modifier
            .height(86.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.card)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        IconBadge(size = 34.dp, bg = iconBg, mark = iconMark, markColor = iconColor, markSize = 20.sp)
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp), color = colors.ink)
            Spacer(Modifier.weight(1f))
            Text(value, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = valueColor)
        }
    }
}

/** Full-width status row: a label on the left and the case status on the right. */
@Composable
private fun StatusSummaryCard(value: String) {
    val colors = EijyoTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.card)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text("当前状态", style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted)
        Spacer(Modifier.weight(1f))
        Text(value, style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp), color = colors.ink)
    }
}

@Composable
private fun IconBadge(size: Dp, bg: Color, mark: String, markColor: Color, markSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(bg),
    ) {
        Text(mark, style = EijyoTheme.typography.labelLarge.copy(fontSize = markSize), color = markColor)
    }
}
