package com.eijyo.tracker.feature.application

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.OnboardingBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.domain.timeline.TimelineDisplayItem
import com.eijyo.tracker.domain.timeline.TimelineDot

private val ShadowTint = Color(0x1A8C5C3D)
private val CoralSoft = Color(0xFFFFD4C8)
private val LavendPill = Color(0xFFE4DBFF)
private val HintBg = Color(0xFFFFF7ED)
private val TimelineLineColor = Color(0xFFF0E3D7)

// UI 验收用假数据预览开关。时间线已接真实数据（TimelineBuilder），关闭。
private const val MOCK_PREVIEW = false
private val mockState = ApplicationUiState(
    officeName = "东京入管",
    statusLabel = "审查中",
    waitDaysLabel = "已提交 128 天",
    stagePillLabel = "等待结果中",
    visaTypeLabel = "技术・人文知识・国际业务",
    pathLabel = "10年居住",
    submittedDateDisplay = "2026.04.12",
    timeline = listOf(
        TimelineDisplayItem(TimelineDot.MINT, "2026.04.12", "已提交申请", "材料已交给入管"),
        TimelineDisplayItem(TimelineDot.SKY, "2026.04.12", "入管受理", "开始进入审查流程"),
        TimelineDisplayItem(TimelineDot.LAVENDER, "预计 2026.09 - 11", "审查结果", "基于公开数据估算"),
        TimelineDisplayItem(TimelineDot.CORAL, "待发生", "通知书 / 明信片 / 补资料", "有新状态会提醒你", isPending = true),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(viewModel: ApplicationViewModel = hiltViewModel()) {
    val realState by viewModel.state.collectAsStateWithLifecycle()
    val state = if (MOCK_PREVIEW) mockState else realState
    val colors = EijyoTheme.colors

    var showAddEvent by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize().background(colors.screen)) {
        OnboardingBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Header()
            Spacer(Modifier.height(14.dp))
            StatusCard(state)
            Spacer(Modifier.height(16.dp))
            TimelineCard(state, onAddEvent = { showAddEvent = true })
            Spacer(Modifier.height(16.dp))
            InfoCard(state)
            Spacer(Modifier.height(8.dp))
            InfoNote()
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAddEvent) {
        AddEventSheet(
            sheetState = sheetState,
            onDismiss = { showAddEvent = false },
            onSupplementReceived = { d, dl, desc ->
                viewModel.addSupplementReceived(d, dl, desc)
                showAddEvent = false
            },
            onSupplementSubmitted = { d ->
                viewModel.addSupplementSubmitted(d)
                showAddEvent = false
            },
            onResult = { type, date ->
                viewModel.recordResult(type, date)
                showAddEvent = false
            },
            onNoticeOrNote = { showAddEvent = false },
        )
    }
}

@Composable
private fun Header() {
    val colors = EijyoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.application_title), style = EijyoTheme.typography.headlineMedium.copy(fontSize = 28.sp), color = colors.ink)
            Text(stringResource(R.string.application_subtitle), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
        }
        DogFace(size = 36.dp)
    }
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    radius: Dp = 26.dp,
    padding: Dp = 20.dp,
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
private fun SmallPill(text: String, bg: Color, textColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bg)
            .height(26.dp)
            .padding(horizontal = 10.dp),
    ) {
        Text(text, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = textColor)
    }
}

@Composable
private fun StatusCard(state: ApplicationUiState) {
    val colors = EijyoTheme.colors
    AppCard(radius = 30.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.application_status_label), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                val statusText = if (state.officeName.isNotEmpty() && state.statusLabel.isNotEmpty())
                    "${state.officeName} · ${state.statusLabel}" else state.statusLabel.ifEmpty { stringResource(R.string.application_no_record) }
                Text(statusText, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 22.sp), color = colors.ink)
                if (state.waitDaysLabel.isNotEmpty() || state.stagePillLabel.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.waitDaysLabel.isNotEmpty()) SmallPill(state.waitDaysLabel, colors.mintWash, colors.mint)
                        if (state.stagePillLabel.isNotEmpty()) SmallPill(state.stagePillLabel, colors.skySoft, colors.skyAccent)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(13.dp)).background(CoralSoft))
        }
    }
}

@Composable
private fun TimelineCard(state: ApplicationUiState, onAddEvent: () -> Unit) {
    val colors = EijyoTheme.colors
    AppCard(radius = 28.dp, padding = 20.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.application_timeline_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 17.sp), color = colors.ink, modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(CoralSoft)
                    .height(26.dp)
                    .padding(horizontal = 10.dp)
                    .clickable { onAddEvent() },
            ) {
                Text(stringResource(R.string.application_add_event), style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = colors.coral)
            }
        }
        Spacer(Modifier.height(14.dp))
        state.timeline.forEachIndexed { index, item ->
            TimelineRow(item, isLast = index == state.timeline.lastIndex)
        }
        if (state.timeline.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(17.dp))
                    .background(HintBg)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    stringResource(R.string.application_timeline_hint),
                    style = EijyoTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    color = colors.coral,
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(item: TimelineDisplayItem, isLast: Boolean) {
    val colors = EijyoTheme.colors
    val dotColor = when (item.dot) {
        TimelineDot.MINT -> colors.mint
        TimelineDot.SKY -> colors.skyAccent
        TimelineDot.LAVENDER -> colors.lavenderAccent
        TimelineDot.CORAL -> colors.coral
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        // Left: dot + vertical line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(dotColor),
            )
            if (!isLast) {
                Canvas(modifier = Modifier.width(1.dp).height(30.dp)) {
                    drawLine(TimelineLineColor, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2.dp.toPx())
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        // Right: date + title + subtitle
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = if (isLast) 0.dp else 2.dp)) {
            Text(
                item.dateLabel,
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = colors.inkMuted,
                modifier = Modifier.width(90.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = EijyoTheme.typography.labelLarge.copy(fontSize = 13.sp), color = if (item.isPending) colors.inkMuted else colors.ink)
                Text(item.subtitle, style = EijyoTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.inkMuted)
            }
        }
    }
}


@Composable
private fun InfoCard(state: ApplicationUiState) {
    val colors = EijyoTheme.colors
    AppCard(radius = 26.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.application_info_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink, modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clip(RoundedCornerShape(13.dp)).background(LavendPill).height(26.dp).padding(horizontal = 10.dp),
            ) {
                Text(stringResource(R.string.application_info_readonly), style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.lavenderAccent)
            }
        }
        Spacer(Modifier.height(14.dp))
        InfoRow(stringResource(R.string.application_info_visa), state.visaTypeLabel.ifEmpty { "—" })
        Spacer(Modifier.height(12.dp))
        InfoRow(stringResource(R.string.application_info_path), state.pathLabel.ifEmpty { "—" })
        Spacer(Modifier.height(12.dp))
        InfoRow(stringResource(R.string.application_info_submit_date), state.submittedDateDisplay.ifEmpty { "—" })
        Spacer(Modifier.height(12.dp))
        InfoRow(stringResource(R.string.application_info_office), state.officeName.ifEmpty { "—" })
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = EijyoTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = colors.inkMuted)
        Spacer(Modifier.weight(1f))
        Text(value, style = EijyoTheme.typography.labelLarge.copy(fontSize = 12.sp), color = colors.ink, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun InfoNote() {
    val colors = EijyoTheme.colors
    Text(
        stringResource(R.string.application_info_note),
        style = EijyoTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = colors.inkMuted,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}
