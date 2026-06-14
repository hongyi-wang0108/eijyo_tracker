package com.eijyo.tracker.feature.data

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                            "数据",
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            color = MacaronPalette.Ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        val subtitle = if (state.officeLabel.isNotBlank())
                            "${state.officeLabel} · 永住相关公开资料"
                        else
                            "永住相关公开资料"
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

            // Official processing period hero card
            item {
                Spacer(Modifier.height(16.dp))
                OfficialHeroCard(
                    state = state,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Backlog / processing card — real e-Stat data
            state.stats?.let { stats ->
                item {
                    Spacer(Modifier.height(16.dp))
                    BacklogCard(
                        officeLabel = state.officeLabel,
                        dataAsOfLabel = state.dataAsOfLabel,
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
                    title = "永住許可申請 官方说明",
                    summary = "申请手续、材料、处理期间",
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
                    title = "出入国在留管理庁 出入国管理統計",
                    summary = "e-Stat 受理处理・许可人数（每月更新）",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

// ── Official processing period hero card ──────────────────────────────────────

@Composable
private fun OfficialHeroCard(
    state: DataUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(30.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        // Paw print decoration top-right
        DataPawPrint(
            color = MacaronPalette.DogPaw,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp)
                .rotate(14f)
                .size(18.dp),
            opacity = 0.18f,
        )

        Column {
            Text(
                "官方标准处理期间",
                style = EijyoTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MacaronPalette.InkMuted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                state.standardRange,
                style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                color = MacaronPalette.Ink,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataPill(
                    text = state.sourceName,
                    bg = MacaronPalette.MintWash,
                    textColor = MacaronPalette.Mint,
                )
                if (state.dataAsOfLabel.isNotBlank()) {
                    DataPill(
                        text = "更新至 ${state.dataAsOfLabel}",
                        bg = MacaronPalette.SkySoft,
                        textColor = MacaronPalette.SkyAccent,
                    )
                }
                if (state.originLabel.isNotBlank()) {
                    DataPill(
                        text = state.originLabel,
                        bg = MacaronPalette.LavenderSoft,
                        textColor = MacaronPalette.LavenderAccent,
                    )
                }
            }
        }
    }
}

// ── Backlog / processing card (real e-Stat) ─────────────────────────────────────

@Composable
private fun BacklogCard(
    officeLabel: String,
    dataAsOfLabel: String,
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
            if (officeLabel.isNotBlank()) "${officeLabel}受理处理情况" else "受理处理情况",
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${stats.latestMonthLabel} · 永住（已扣除管内支局）",
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(
                label = "积压待处理",
                value = formatCount(stats.pending),
                accent = MacaronPalette.Coral,
                modifier = Modifier.weight(1f),
            )
            StatBox(
                label = "本月处理",
                value = formatCount(stats.processed),
                accent = MacaronPalette.Mint,
                modifier = Modifier.weight(1f),
            )
            StatBox(
                label = "本月新申请",
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
                    "按实际处理速度估算，目前提交大约需要 ${formatMonths(months)} 个月出结果。" +
                        "仅为整体参考，非你个案的实际进度。",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MacaronPalette.Ink,
                )
            }
        }

        if (stats.bureauLabel != null && stats.bureauPending != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "参考：${stats.bureauLabel}积压 ${formatCount(stats.bureauPending)}（含支局，整体看更快）",
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
            if (hasTrend) state.trendTitle else "处理参考趋势",
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hasTrend) "数据来源：${state.sourceName}（单位：${state.trendUnit}）"
            else "暂无所选地区的公开趋势数据",
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
        Spacer(Modifier.height(16.dp))

        if (hasTrend) {
            TrendBarChart(
                bars = state.trend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "未指定地区或暂无可确认数据，不展示虚构数字",
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEachIndexed { index, bar ->
            val frac = (bar.value.toFloat() / maxValue).coerceIn(0.04f, 1f)
            val isLatest = index == bars.lastIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    formatCount(bar.value),
                    style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                    color = MacaronPalette.InkMuted,
                    maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(frac)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (isLatest) MacaronPalette.Mint
                            else MacaronPalette.MintContainer.copy(alpha = 0.55f)
                        ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    bar.label,
                    style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium),
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
    val title = if (officeLabel.isNotBlank()) "${officeLabel}地区信息" else "地区信息"

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
                "地区参考",
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
                "用于匹配你的提交地区，展示可确认的官方资料和处理参考。",
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
            "申请路径说明",
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
            "重点确认：纳税、年金、保险、收入稳定性",
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

@Composable
private fun DataPawPrint(
    color: Color,
    modifier: Modifier = Modifier,
    opacity: Float = 0.18f,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(8.dp, 6.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = opacity)),
        )
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            repeat(3) {
                Box(
                    Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = opacity)),
                )
            }
        }
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
