package com.eijyo.tracker.feature.prediction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.OnboardingBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette

private val ShadowTint = Color(0x1A8C5C3D)
private val RingTrack = Color(0xFFF2EEE7)
private val LemonBadge = Color(0xFFFFF0B8)

// TEMP（UI 验收用）：用假数据预览有预测时的样子。验收完改回 false。
private const val MOCK_PREVIEW = true
private val mockState = PredictionDetailUiState(
    available = true,
    normalRange = "2026年9月中旬 - 11月上旬",
    progressPercent = 71,
    confidenceLabel = "中高",
    office = "东京入管",
    optimisticRange = "9月上旬",
    conservativeRange = "11月下旬",
    processingRange = "4 - 6 个月",
    waitDays = 128,
    pathLabel = "技人国 · 10年路径",
    supplementStatus = "未发生",
    sourceName = "出入国在留管理庁",
    sourceUpdated = "2026年5月更新",
)

@Composable
fun PredictionDetailScreen(
    onBack: () -> Unit,
    viewModel: PredictionDetailViewModel = hiltViewModel(),
) {
    val realState by viewModel.state.collectAsStateWithLifecycle()
    val state = if (MOCK_PREVIEW) mockState else realState
    val colors = EijyoTheme.colors

    Box(modifier = Modifier.fillMaxSize().background(colors.screen)) {
        OnboardingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            TopNav(onBack)
            Spacer(Modifier.height(20.dp))
            HeroCard(state)
            Spacer(Modifier.height(16.dp))
            ScenarioCard(state)
            Spacer(Modifier.height(16.dp))
            FactorsCard(state)
            Spacer(Modifier.height(16.dp))
            DataBasisCard(state)
            Spacer(Modifier.height(16.dp))
            DisclaimerCard()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopNav(onBack: () -> Unit) {
    val colors = EijyoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .shadow(6.dp, RoundedCornerShape(19.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
                .clip(RoundedCornerShape(19.dp))
                .background(colors.card)
                .clickable(onClick = onBack),
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回首页", tint = colors.ink, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text("预测详情", style = EijyoTheme.typography.headlineMedium.copy(fontSize = 22.sp), color = colors.ink)
        Spacer(Modifier.weight(1f))
        DogFace(size = 34.dp)
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    radius: Dp = 26.dp,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = EijyoTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(radius), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(radius))
            .background(colors.card)
            .padding(padding),
        content = content,
    )
}

@Composable
private fun Pill(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(bg).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = textColor)
    }
}

@Composable
private fun HeroCard(state: PredictionDetailUiState) {
    val colors = EijyoTheme.colors
    DetailCard(radius = 30.dp, padding = 22.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("预计结果区间", style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
                Spacer(Modifier.height(10.dp))
                val parts = state.normalRange.split(" - ", " – ", "-").map { it.trim() }
                Text(parts.getOrElse(0) { state.normalRange }, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 29.sp), color = colors.ink)
                if (parts.size > 1) {
                    Text("- ${parts[1]}", style = EijyoTheme.typography.headlineMedium.copy(fontSize = 29.sp), color = colors.ink)
                }
            }
            DetailRing(percent = state.progressPercent)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("置信度 ${state.confidenceLabel}", colors.pink, colors.pinkAccent)
            Pill(state.office, colors.skySoft, colors.skyAccent)
        }
    }
}

@Composable
private fun DetailRing(percent: Int, ringSize: Dp = 66.dp) {
    val colors = EijyoTheme.colors
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(RingTrack, -90f, 360f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke))
            drawArc(colors.mint, -90f, 360f * percent / 100f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text("$percent%", style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
    }
}

@Composable
private fun ScenarioCard(state: PredictionDetailUiState) {
    val colors = EijyoTheme.colors
    DetailCard {
        Text("三种参考区间", style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Scenario(Modifier.weight(1f).fillMaxHeight(), "乐观", state.optimisticRange, colors.mintWash, colors.mint)
            Scenario(Modifier.weight(1f).fillMaxHeight(), "正常", state.normalRange, colors.skySoft, colors.skyAccent)
            Scenario(Modifier.weight(1f).fillMaxHeight(), "保守", state.conservativeRange, LemonBadge, colors.lemonAccent)
        }
    }
}

@Composable
private fun Scenario(modifier: Modifier, label: String, date: String, bg: Color, labelColor: Color) {
    val colors = EijyoTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = labelColor)
        Spacer(Modifier.height(6.dp))
        Text(date, style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp), color = colors.ink)
    }
}

@Composable
private fun FactorsCard(state: PredictionDetailUiState) {
    val colors = EijyoTheme.colors
    DetailCard {
        Text("影响预测的因素", style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
        Spacer(Modifier.height(14.dp))
        val rows = listOf(
            Triple(colors.mint, "官方处理期间", state.processingRange),
            Triple(colors.coral, "当前等待天数", "${state.waitDays} 天"),
            Triple(colors.skyAccent, "申请路径", state.pathLabel),
            Triple(colors.lavenderAccent, "补资料状态", state.supplementStatus),
        )
        rows.forEachIndexed { i, (dot, label, value) ->
            if (i > 0) Spacer(Modifier.height(13.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(dot))
                Spacer(Modifier.width(12.dp))
                Text(label, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = colors.inkMuted)
                Spacer(Modifier.weight(1f))
                Text(value, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = colors.ink)
            }
        }
    }
}

@Composable
private fun DataBasisCard(state: PredictionDetailUiState) {
    val colors = EijyoTheme.colors
    DetailCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("公开数据依据", style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
                Spacer(Modifier.height(8.dp))
                Text("来源：${state.sourceName} / 官方公开资料", style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                Text("如无地区通过人数，不展示虚构数字。", style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.coral)
            }
            TinyUpChart(modifier = Modifier.size(width = 48.dp, height = 40.dp))
        }
    }
}

/** Small ascending line: "processing reference trend" placeholder per the design. */
@Composable
private fun TinyUpChart(modifier: Modifier = Modifier) {
    val colors = EijyoTheme.colors
    Canvas(modifier = modifier) {
        val pts = listOf(0.1f to 0.85f, 0.4f to 0.6f, 0.7f to 0.45f, 0.95f to 0.2f)
        val mapped = pts.map { Offset(it.first * size.width, it.second * size.height) }
        for (i in 0 until mapped.size - 1) {
            drawLine(colors.mint, mapped[i], mapped[i + 1], strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
        mapped.forEach { drawCircle(colors.mint, radius = 2.5.dp.toPx(), center = it) }
    }
}

@Composable
private fun DisclaimerCard() {
    val colors = EijyoTheme.colors
    DetailCard(radius = 24.dp, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(LemonBadge),
            ) {
                Text("!", style = EijyoTheme.typography.labelLarge.copy(fontSize = 13.sp), color = colors.lemonAccent)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                "预测仅供参考，不构成法律意见，也不保证结果。",
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        }
    }
}
