package com.eijyo.tracker.feature.data

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MacaronPalette.Cream),
    ) {
        // Background blobs (fixed)
        Box(
            Modifier
                .offset((-58).dp, (-42).dp)
                .size(184.dp)
                .clip(CircleShape)
                .background(MacaronPalette.MintSoft),
        )
        Box(
            Modifier
                .offset(294.dp, 52.dp)
                .size(142.dp)
                .clip(CircleShape)
                .background(MacaronPalette.Peach),
        )
        Box(
            Modifier
                .offset(268.dp, 608.dp)
                .size(154.dp)
                .clip(CircleShape)
                .background(MacaronPalette.LavenderSoft),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, end = 24.dp),
                ) {
                    Column {
                        Text(
                            stringResource(R.string.data_title),
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            color = MacaronPalette.Ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        val subtitle = if (state.officeLabel.isNotBlank())
                            stringResource(R.string.data_subtitle_with_office, state.officeLabel)
                        else
                            stringResource(R.string.data_subtitle_no_office)
                        Text(
                            subtitle,
                            style = EijyoTheme.typography.bodyMedium,
                            color = MacaronPalette.InkMuted,
                        )
                    }
                    DogFace(
                        modifier = Modifier.align(Alignment.TopEnd),
                        size = 36.dp,
                    )
                }
            }

            // Backlog / processing card — real e-Stat data
            state.stats?.let { stats ->
                item {
                    Spacer(Modifier.height(16.dp))
                    BacklogCard(
                        officeLabel = state.officeLabel,
                        stats = stats,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            // Trend chart card
            item {
                Spacer(Modifier.height(16.dp))
                TrendChartCard(
                    state = state,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Region info card
            item {
                Spacer(Modifier.height(16.dp))
                RegionInfoCard(
                    officeLabel = state.officeLabel,
                    regionalNote = state.regionalNote,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Application path info card
            if (state.visaTypeLabel.isNotBlank() || state.pathLabel.isNotBlank()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    PathInfoCard(
                        visaTypeLabel = state.visaTypeLabel,
                        pathLabel = state.pathLabel,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            // Source card 1 — 官方说明
            item {
                Spacer(Modifier.height(16.dp))
                SourceCard(
                    iconBg = MacaronPalette.MintWash,
                    iconText = "✓",
                    iconTextColor = MacaronPalette.Mint,
                    title = stringResource(R.string.data_source1_title),
                    summary = stringResource(R.string.data_source1_summary),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Source card 2 — 公开统计
            item {
                Spacer(Modifier.height(16.dp))
                SourceCard(
                    iconBg = MacaronPalette.SkySoft,
                    iconText = "i",
                    iconTextColor = MacaronPalette.SkyAccent,
                    title = stringResource(R.string.data_source2_title),
                    summary = stringResource(R.string.data_source2_summary),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

// ── Backlog / processing card (real e-Stat) ─────────────────────────────────────

@Composable
private fun BacklogCard(
    officeLabel: String,
    stats: OfficeStats,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(28.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            if (officeLabel.isNotBlank()) stringResource(R.string.data_backlog_title_with_office, officeLabel)
            else stringResource(R.string.data_backlog_title_no_office),
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(R.string.data_backlog_subtitle, stats.latestMonthLabel),
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(
                label = stringResource(R.string.data_stat_pending),
                value = formatCount(stats.pending),
                accent = MacaronPalette.Coral,
                modifier = Modifier.weight(1f),
            )
            StatBox(
                label = stringResource(R.string.data_stat_processed),
                value = formatCount(stats.processed),
                accent = MacaronPalette.Mint,
                modifier = Modifier.weight(1f),
            )
            StatBox(
                label = stringResource(R.string.data_stat_received),
                value = formatCount(stats.received),
                accent = MacaronPalette.SkyAccent,
                modifier = Modifier.weight(1f),
            )
        }

        stats.queueMonths?.let { months ->
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MacaronPalette.MintWash)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    stringResource(R.string.data_queue_estimate, formatMonths(months)),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MacaronPalette.Ink,
                )
            }
        }

        if (stats.bureauLabel != null && stats.bureauPending != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.data_bureau_reference, stats.bureauLabel, formatCount(stats.bureauPending)),
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = MacaronPalette.InkMuted,
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MacaronPalette.Cream)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            value,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = accent,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MacaronPalette.InkMuted,
        )
    }
}

// ── Trend chart card ───────────────────────────────────────────────────────────

@Composable
private fun TrendChartCard(
    state: DataUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(28.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        val hasTrend = state.trend.isNotEmpty()
        Text(
            if (hasTrend) state.trendTitle else stringResource(R.string.data_trend_fallback_title),
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hasTrend) stringResource(R.string.data_trend_subtitle_with_data, state.sourceName, state.trendUnit)
            else stringResource(R.string.data_trend_no_data_subtitle),
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
        Spacer(Modifier.height(16.dp))

        if (hasTrend) {
            TrendBarChart(
                bars = state.trend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(146.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.data_trend_empty_hint),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MacaronPalette.InkMuted,
                )
            }
        }
    }
}

@Composable
private fun TrendBarChart(
    bars: List<TrendBar>,
    modifier: Modifier = Modifier,
) {
    val maxValue = (bars.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEachIndexed { index, bar ->
            val frac = (bar.value.toFloat() / maxValue).coerceIn(0.04f, 1f)
            val isLatest = index == bars.lastIndex
            Column(
                modifier = Modifier.width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        formatCount(bar.value),
                        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                        color = MacaronPalette.InkMuted,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((84.dp * frac).coerceAtLeast(10.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (isLatest) MacaronPalette.Mint
                                else MacaronPalette.MintContainer.copy(alpha = 0.55f)
                            ),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    bar.label,
                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = MacaronPalette.InkMuted,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Region info card ───────────────────────────────────────────────────────────

@Composable
private fun RegionInfoCard(
    officeLabel: String,
    regionalNote: String,
    modifier: Modifier = Modifier,
) {
    val title = if (officeLabel.isNotBlank())
        stringResource(R.string.data_region_title_with_office, officeLabel)
    else
        stringResource(R.string.data_region_title_no_office)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(26.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        // "地区参考" chip — top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFFFFD4C8))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.data_region_chip),
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = MacaronPalette.Coral,
            )
        }

        Column(modifier = Modifier.padding(end = 90.dp)) {
            Text(
                title,
                style = EijyoTheme.typography.titleMedium,
                color = MacaronPalette.Ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.data_region_description),
                style = EijyoTheme.typography.labelMedium,
                color = MacaronPalette.InkMuted,
            )
            if (regionalNote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    regionalNote,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MacaronPalette.InkMuted,
                )
            }
        }
    }
}

// ── Application path info card ─────────────────────────────────────────────────

@Composable
private fun PathInfoCard(
    visaTypeLabel: String,
    pathLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(26.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            stringResource(R.string.data_path_title),
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(8.dp))
        if (visaTypeLabel.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DataPill(
                    text = visaTypeLabel,
                    bg = MacaronPalette.LavenderSoft,
                    textColor = MacaronPalette.LavenderAccent,
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        if (pathLabel.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DataPill(
                    text = pathLabel,
                    bg = MacaronPalette.SkySoft,
                    textColor = MacaronPalette.SkyAccent,
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(
            stringResource(R.string.data_path_key_points),
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
    }
}

// ── Source card ────────────────────────────────────────────────────────────────

@Composable
private fun SourceCard(
    iconBg: Color,
    iconText: String,
    iconTextColor: Color,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                iconText,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = iconTextColor,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                style = EijyoTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MacaronPalette.Ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                summary,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = MacaronPalette.InkMuted,
            )
        }
    }
}

// ── Shared small components ────────────────────────────────────────────────────

@Composable
private fun DataPill(
    text: String,
    bg: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text,
            style = EijyoTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

// ── Formatting helpers ──────────────────────────────────────────────────────────

/** 46300 → "4.6万"; 17903 → "1.8万"; 565 → "565". Compact for cards (1 decimal). */
private fun formatCount(n: Int): String = when {
    n >= 10_000 -> {
        val wan = kotlin.math.round(n / 1000.0) / 10.0  // round to 1 decimal of 万
        if (wan == wan.toInt().toDouble()) "${wan.toInt()}万" else "${wan}万"
    }
    else -> n.toString()
}

/** 14.67 → "14.7". */
private fun formatMonths(m: Double): String =
    (kotlin.math.round(m * 10) / 10.0).toString()
