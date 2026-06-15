package com.eijyo.tracker.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.WelcomeBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.domain.timeline.TimelineDot

private val ShadowTint = Color(0x1C8C5C3D)
private val CandyTrack = Color(0xFFFCE2EA)

// TEMP（UI 验收用）：仅为预览「审查中」填充态而补齐**缺失**字段；真实已有的数据
// （昵称、office、已提交天数、材料数等）一律保持原值不动。验收完改回 false。
private const val MOCK_PREVIEW = false

@Composable
fun HomeScreen(
    onOpenPredictionDetail: () -> Unit = {},
    onOpenRiskDetail: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val realState by viewModel.state.collectAsStateWithLifecycle()
    val state = if (!MOCK_PREVIEW) realState else realState.copy(
        status = ApplicationStatus.REVIEWING,
        statusSummary = realState.statusSummary.takeIf { "·" in it } ?: "东京入管 · 审查中",
        waitDays = realState.waitDays ?: 128,
        predictionRange = realState.predictionRange ?: "2026年9月中旬 - 11月上旬",
        confidenceLabel = realState.confidenceLabel ?: "中高",
        progressPercent = realState.progressPercent ?: 71,
        riskLevel = realState.riskLevel ?: RiskLevel.LOW,
        documentsTotal = realState.documentsTotal.takeIf { it > 0 } ?: 31,
        documentsPrepared = if (realState.documentsTotal > 0) realState.documentsPrepared else 23,
    )
    val colors = EijyoTheme.colors
    val officeFallback = stringResource(R.string.home_data_title_fallback)

    Box(modifier = Modifier.fillMaxSize().background(colors.screen)) {
        WelcomeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(state)
            PredictionCard(state, onClick = onOpenPredictionDetail)
            PublicDataCard(office = officeName(state, officeFallback), state = state)
            TimelineCard(state)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                RiskMiniCard(modifier = Modifier.weight(1f), level = state.riskLevel, onClick = onOpenRiskDetail)
                MaterialMiniCard(
                    modifier = Modifier.weight(1f),
                    prepared = state.documentsPrepared,
                    total = state.documentsTotal,
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * Best-effort office name out of the "东京入管 · 审查中" status summary. Only treats the
 * prefix as an office when it actually names one; otherwise (e.g. "准备中") falls back to a
 * generic label so the card never reads "准备中数据".
 */
private fun officeName(state: HomeUiState, fallback: String): String {
    val prefix = state.statusSummary.substringBefore(" ·").substringBefore("·").trim()
    return if (prefix.contains("入管")) prefix else fallback
}

@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    padding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = EijyoTheme.colors
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(radius), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(radius))
            .background(colors.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
private fun Header(state: HomeUiState) {
    val colors = EijyoTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1: greeting (left) + 已提交 N 天 pill (right, only when reviewing).
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                state.greeting,
                style = EijyoTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            if (state.waitDays != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.mintWash)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        stringResource(R.string.home_header_wait_days, state.waitDays),
                        style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = colors.mint,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // Row 2: case status (left) + compact dog-assistant card (right). Top-aligned so
        // the status sits tight under the greeting instead of being centered in the tall card.
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Text(
                state.statusSummary.ifBlank { stringResource(R.string.home_header_preparing_fallback) },
                style = EijyoTheme.typography.bodyMedium,
                color = colors.inkMuted,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
            )
            AssistantCard()
        }
    }
}

@Composable
private fun AssistantCard() {
    val colors = EijyoTheme.colors
    HomeCard(modifier = Modifier.width(200.dp), radius = 24.dp, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssistantDog()
            Spacer(Modifier.width(10.dp))
            Column {
                Text(stringResource(R.string.home_assistant_title), style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.ink)
                Spacer(Modifier.height(2.dp))
                Text(stringResource(R.string.home_assistant_subtitle), style = EijyoTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.inkMuted)
            }
        }
    }
}

/** Small lively dog: pastel blobs behind a dog face, with two sparkle dots. */
@Composable
private fun AssistantDog(boxSize: Dp = 46.dp) {
    Box(modifier = Modifier.size(boxSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(MacaronPalette.MintContainer, Offset(size.width * 0.06f, size.height * 0.18f), Size(size.width * 0.62f, size.height * 0.62f))
            drawOval(Color(0xFFFFD4C8), Offset(size.width * 0.52f, size.height * 0.10f), Size(size.width * 0.40f, size.height * 0.40f))
        }
        DogFace(size = boxSize * 0.92f)
        Text("·", style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = MacaronPalette.PinkAccent, modifier = Modifier.offset(x = boxSize * 0.30f, y = -boxSize * 0.30f))
        Text("·", style = EijyoTheme.typography.labelLarge.copy(fontSize = 12.sp), color = MacaronPalette.LavenderAccent, modifier = Modifier.offset(x = boxSize * 0.40f, y = -boxSize * 0.12f))
    }
}

@Composable
private fun PredictionCard(state: HomeUiState, onClick: () -> Unit) {
    when (state.status) {
        ApplicationStatus.COMPLETED -> ResultHeroCard(state)
        ApplicationStatus.PREPARING -> PreparingHeroCard(state)
        ApplicationStatus.REVIEWING ->
            if (state.predictionRange != null) ReviewingHeroCard(state, onClick)
            else PreparingHeroCard(state) // reviewing but date unknown → guide to fill it in
    }
}

/** REVIEWING: predicted result window + progress ring + confidence. */
@Composable
private fun ReviewingHeroCard(state: HomeUiState, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    HomeCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp, padding = 22.dp, onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_reviewing_title), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
                Spacer(Modifier.height(8.dp))
                val parts = state.predictionRange!!.split(" - ", " – ", "-").map { it.trim() }
                Text(parts.getOrElse(0) { state.predictionRange!! }, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 28.sp), color = colors.ink)
                if (parts.size > 1) {
                    Text("- ${parts[1]}", style = EijyoTheme.typography.headlineMedium.copy(fontSize = 28.sp), color = colors.ink)
                }
            }
            if (state.progressPercent != null) {
                ProgressRing(percent = state.progressPercent)
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(CandyTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(((state.progressPercent ?: 0) / 100f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(colors.mintContainer),
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.home_reviewing_footnote),
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            state.confidenceLabel?.let {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.pink)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(stringResource(R.string.home_reviewing_confidence, it), style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = colors.pinkAccent)
                }
            }
        }
    }
}

/** PREPARING: guide the user toward completing materials & risk check (no fake prediction). */
@Composable
private fun PreparingHeroCard(state: HomeUiState) {
    val colors = EijyoTheme.colors
    HomeCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp, padding = 22.dp) {
        Text(stringResource(R.string.home_preparing_title), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_preparing_subtitle),
            style = EijyoTheme.typography.headlineMedium.copy(fontSize = 26.sp),
            color = colors.ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            state.predictionPlaceholder ?: stringResource(R.string.home_preparing_date_hint),
            style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = colors.inkMuted,
        )
        if (state.documentsTotal > 0) {
            Spacer(Modifier.height(16.dp))
            val frac = state.documentsPrepared.toFloat() / state.documentsTotal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(CandyTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(frac.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(colors.mintContainer),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_preparing_docs_progress, state.documentsPrepared, state.documentsTotal),
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        }
    }
}

/** COMPLETED: show the recorded final result. */
@Composable
private fun ResultHeroCard(state: HomeUiState) {
    val colors = EijyoTheme.colors
    val accent = if (state.resultApproved) colors.mint else colors.coral
    HomeCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp, padding = 22.dp) {
        Text(stringResource(R.string.home_result_title), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
        Spacer(Modifier.height(8.dp))
        Text(
            state.resultLabel.ifBlank { stringResource(R.string.home_result_fallback) },
            style = EijyoTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            color = accent,
        )
        if (state.resultDate.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_result_date, state.resultDate),
                style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = colors.inkMuted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(if (state.resultApproved) R.string.home_result_approved_body else R.string.home_result_other_body),
            style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun ProgressRing(percent: Int, ringSize: Dp = 66.dp) {
    val colors = EijyoTheme.colors
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = colors.mintWash,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = colors.mint,
                startAngle = -90f, sweepAngle = 360f * percent / 100f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
        }
        Text("$percent%", style = EijyoTheme.typography.labelLarge.copy(fontSize = 17.sp), color = colors.ink)
    }
}

@Composable
private fun PublicDataCard(office: String, state: HomeUiState) {
    val colors = EijyoTheme.colors
    val updateLabel = if (state.publicDataAsOf.isNotBlank())
        stringResource(R.string.home_data_update_label, state.publicDataAsOf)
    else
        stringResource(R.string.home_data_official_stats)
    HomeCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_data_title_fmt, office), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
                Spacer(Modifier.height(4.dp))
                Text(updateLabel, style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp), color = colors.inkMuted)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(colors.mintWash)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(stringResource(R.string.home_data_badge), style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp), color = colors.mint)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                if (state.backlogLabel.isNotBlank()) {
                    Text(stringResource(R.string.home_data_backlog_label), style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.inkMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(state.backlogLabel, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 22.sp), color = colors.ink)
                } else {
                    Text(stringResource(R.string.home_data_official_stats), style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.inkMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.home_data_view_more), style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted)
                }
            }
            if (state.miniTrend.isNotEmpty()) {
                MiniBarChart(values = state.miniTrend, modifier = Modifier.width(140.dp).height(48.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(stringResource(R.string.home_data_footnote), style = EijyoTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.inkMuted)
        }
    }
}

@Composable
private fun MiniBarChart(values: List<Int>, modifier: Modifier = Modifier) {
    val colors = EijyoTheme.colors
    val palette = listOf(colors.skySoft, colors.lavenderSoft, colors.mintContainer, colors.peach, colors.mint)
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, v ->
            val frac = (v.toFloat() / max).coerceIn(0.08f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(frac)
                    .clip(RoundedCornerShape(5.dp))
                    .background(palette[i % palette.size]),
            )
        }
    }
}

@Composable
private fun TimelineCard(state: HomeUiState) {
    val colors = EijyoTheme.colors
    HomeCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp) {
        Text(stringResource(R.string.home_timeline_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
        Spacer(Modifier.height(12.dp))
        if (state.timeline.isEmpty()) {
            Text(
                stringResource(R.string.home_timeline_empty),
                style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        } else {
            state.timeline.forEachIndexed { i, item ->
                val dotColor = when (item.dot) {
                    TimelineDot.MINT -> colors.mint
                    TimelineDot.SKY -> colors.skyAccent
                    TimelineDot.LAVENDER -> colors.lavenderAccent
                    TimelineDot.CORAL -> colors.coral
                }
                val titleColor = if (item.isPending) colors.inkMuted else colors.ink
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (item.isPending) colors.border else dotColor),
                        )
                        if (i < state.timeline.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(16.dp)
                                    .background(colors.border),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.dateLabel,
                        style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = colors.inkMuted,
                        modifier = Modifier.width(92.dp),
                    )
                    Text(
                        item.title,
                        style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = titleColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskMiniCard(modifier: Modifier, level: RiskLevel?, onClick: () -> Unit = {}) {
    val colors = EijyoTheme.colors
    val (badgeBg, badgeMark, badgeColor, title) = when (level) {
        RiskLevel.LOW -> Quad(colors.mintWash, "✓", colors.mint, stringResource(R.string.home_risk_low))
        RiskLevel.MEDIUM -> Quad(colors.lemonSoft, "!", colors.lemonAccent, stringResource(R.string.home_risk_medium))
        RiskLevel.HIGH -> Quad(colors.peach, "!", colors.coral, stringResource(R.string.home_risk_high))
        null -> Quad(colors.mintWash, "·", colors.inkMuted, stringResource(R.string.home_risk_unknown))
    }
    HomeCard(modifier = modifier, radius = 24.dp, padding = 14.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(badgeBg, badgeMark, badgeColor)
            Spacer(Modifier.width(10.dp))
            Text(title, style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp), color = colors.ink)
        }
    }
}

@Composable
private fun MaterialMiniCard(modifier: Modifier, prepared: Int, total: Int) {
    val colors = EijyoTheme.colors
    HomeCard(modifier = modifier, radius = 24.dp, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(colors.lemonSoft, "□", colors.lemonAccent)
            Spacer(Modifier.width(10.dp))
            Text(
                if (total == 0) stringResource(R.string.home_material_empty)
                else stringResource(R.string.home_material_count, prepared, total),
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = colors.ink,
            )
        }
    }
}

@Composable
private fun Badge(bg: Color, mark: String, markColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(bg),
    ) {
        Text(mark, style = EijyoTheme.typography.labelLarge.copy(fontSize = 18.sp), color = markColor)
    }
}

private data class Quad(val bg: Color, val mark: String, val markColor: Color, val title: String)
