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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette
import com.eijyo.tracker.data.staticdata.PublicData

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
                    data = state.publicData,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Trend chart card
            item {
                Spacer(Modifier.height(16.dp))
                TrendChartCard(
                    data = state.publicData,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Region info card
            item {
                Spacer(Modifier.height(16.dp))
                RegionInfoCard(
                    officeLabel = state.officeLabel,
                    regionalNote = state.publicData.regionalNote,
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
                    title = "公开统计资料",
                    summary = "只展示可验证来源和更新时间",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

// ── Official processing period hero card ──────────────────────────────────────

@Composable
private fun OfficialHeroCard(
    data: PublicData,
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
                data.standardProcessingRange,
                style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                color = MacaronPalette.Ink,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataPill(
                    text = data.sourceName,
                    bg = MacaronPalette.MintWash,
                    textColor = MacaronPalette.Mint,
                )
                DataPill(
                    text = data.sourceUpdatedAt,
                    bg = MacaronPalette.SkySoft,
                    textColor = MacaronPalette.SkyAccent,
                )
            }
        }
    }
}

// ── Trend chart card ───────────────────────────────────────────────────────────

@Composable
private fun TrendChartCard(
    data: PublicData,
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
            "处理参考趋势",
            style = EijyoTheme.typography.titleMedium,
            color = MacaronPalette.Ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "仅展示官方可确认数据",
            style = EijyoTheme.typography.labelMedium,
            color = MacaronPalette.InkMuted,
        )
        Spacer(Modifier.height(16.dp))
        ProcessingRangeChart(
            minMonths = data.standardProcessingMinMonths,
            maxMonths = data.standardProcessingMaxMonths,
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "如无地区通过人数，不展示虚构数字",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = MacaronPalette.Coral,
        )
    }
}

@Composable
private fun ProcessingRangeChart(
    minMonths: Int,
    maxMonths: Int,
    modifier: Modifier = Modifier,
) {
    val totalMonths = 8
    val bars = (1..totalMonths).map { month ->
        val inRange = month in minMonths..maxMonths
        val height = when {
            month < minMonths -> 0.15f + (month.toFloat() / minMonths) * 0.45f
            inRange -> when (month - minMonths) {
                0 -> 0.75f
                1 -> 1.0f
                else -> 0.85f
            }
            else -> (0.7f - (month - maxMonths) * 0.2f).coerceAtLeast(0.15f)
        }
        inRange to height
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { (inRange, frac) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(frac)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (inRange) MacaronPalette.Mint
                        else MacaronPalette.MintContainer.copy(alpha = 0.45f)
                    ),
            )
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
