package com.eijyo.tracker.feature.risk

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.OnboardingBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.data.model.RiskLevel

private val ShadowTint = Color(0x1A8C5C3D)
private val LemonBadge = Color(0xFFFFF0B8)
private val CoralSoft = Color(0xFFFFD4C8)

// UI 验收用假数据预览开关。已接 RiskEngine 真实评估，关闭。
private const val MOCK_PREVIEW = false
private val mockState = RiskDetailUiState(
    available = true,
    levelLabel = "低风险",
    riskLevel = RiskLevel.LOW,
    summary = "纳税、年金、保险状态均已确认",
    updatedLabel = "刚刚更新",
    sections = listOf(
        RiskSectionItem("素行", "交通/入管记录待持续确认", "无明显问题", SectionStatus.OK),
        RiskSectionItem("生计", "年收与扶养人数匹配", "稳定", SectionStatus.INFO),
        RiskSectionItem("公益性", "纳税・年金・保险正常", "已确认", SectionStatus.CHECKED),
    ),
    recommendations = listOf("建议进入材料页确认住民税纳税证明书和年金记录是否已上传。"),
    recommendationNote = "这会影响材料完整度和预测置信度。",
)

@Composable
fun RiskDetailScreen(
    onBack: () -> Unit,
    viewModel: RiskDetailViewModel = hiltViewModel(),
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
            Spacer(Modifier.height(16.dp))
            HeroCard(state)
            Spacer(Modifier.height(16.dp))
            FactorsCard(state)
            Spacer(Modifier.height(16.dp))
            RecommendationCard(state)
            Spacer(Modifier.height(16.dp))
            SourceCard(state)
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
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.risk_back_cd), tint = colors.ink, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.risk_title), style = EijyoTheme.typography.headlineMedium.copy(fontSize = 22.sp), color = colors.ink)
        Spacer(Modifier.weight(1f))
        DogFace(size = 34.dp)
    }
}

@Composable
private fun RiskCard(
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
private fun SmallPill(text: String, bg: Color, textColor: Color, width: Dp = 96.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .size(width = width, height = 28.dp),
    ) {
        Text(text, style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp), color = textColor)
    }
}

@Composable
private fun HeroCard(state: RiskDetailUiState) {
    val colors = EijyoTheme.colors
    val levelColor = when (state.riskLevel) {
        RiskLevel.LOW -> colors.mint
        RiskLevel.MEDIUM -> colors.lemonAccent
        RiskLevel.HIGH -> colors.coral
    }
    RiskCard(radius = 30.dp, padding = 22.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.risk_hero_label), style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp), color = colors.inkMuted)
            Spacer(Modifier.height(8.dp))
            Text(state.levelLabel, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 36.sp), color = levelColor)
            Spacer(Modifier.height(8.dp))
            Text(state.summary, style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallPill(stringResource(R.string.risk_not_legal), LemonBadge, colors.lemonAccent, 96.dp)
                SmallPill(state.updatedLabel, colors.skySoft, colors.skyAccent, 82.dp)
            }
        }
    }
}

@Composable
private fun FactorsCard(state: RiskDetailUiState) {
    val colors = EijyoTheme.colors
    RiskCard {
        Text(stringResource(R.string.risk_factors_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
        Spacer(Modifier.height(14.dp))
        state.sections.forEachIndexed { i, section ->
            if (i > 0) Spacer(Modifier.height(14.dp))
            SectionRow(section)
        }
    }
}

@Composable
private fun SectionRow(item: RiskSectionItem) {
    val colors = EijyoTheme.colors
    val (circleBg, markText, markColor, pillBg, pillText) = when (item.status) {
        SectionStatus.OK -> SectionColors(colors.mintWash, "✓", colors.mint, colors.mintWash, colors.mint)
        SectionStatus.INFO -> SectionColors(colors.skySoft, "i", colors.skyAccent, colors.skySoft, colors.skyAccent)
        SectionStatus.CHECKED -> SectionColors(LemonBadge, "✓", colors.lemonAccent, LemonBadge, colors.lemonAccent)
        SectionStatus.WARN -> SectionColors(CoralSoft, "!", colors.coral, CoralSoft, colors.coral)
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(circleBg),
        ) {
            Text(markText, style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = markColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp), color = colors.ink)
            Text(item.subtitle, style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp), color = colors.inkMuted)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(pillBg)
                .height(24.dp)
                .padding(horizontal = 10.dp),
        ) {
            Text(item.statusLabel, style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = pillText)
        }
    }
}

private data class SectionColors(
    val circleBg: Color,
    val markText: String,
    val markColor: Color,
    val pillBg: Color,
    val pillText: Color,
)

@Composable
private fun RecommendationCard(state: RiskDetailUiState) {
    val colors = EijyoTheme.colors
    RiskCard {
        Text(stringResource(R.string.risk_recommendation_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink)
        Spacer(Modifier.height(14.dp))
        if (state.recommendations.isEmpty()) {
            Text(stringResource(R.string.risk_recommendation_empty), style = EijyoTheme.typography.labelSmall.copy(fontSize = 13.sp), color = colors.inkMuted)
        } else {
            state.recommendations.forEach { rec ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(CoralSoft),
                    ) {
                        Text("!", style = EijyoTheme.typography.labelLarge.copy(fontSize = 18.sp), color = colors.coral)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec, style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp), color = colors.ink)
                        if (state.recommendationNote.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(state.recommendationNote, style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp), color = colors.inkMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceCard(state: RiskDetailUiState) {
    val colors = EijyoTheme.colors
    RiskCard(radius = 24.dp, padding = 18.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.risk_source_title), style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp), color = colors.ink, modifier = Modifier.weight(1f))
            Text(state.modelType, style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = colors.lavenderAccent)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.risk_source_description),
            style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = colors.inkMuted,
        )
        if (state.sourceSummary.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                state.sourceSummary,
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun DisclaimerCard() {
    val colors = EijyoTheme.colors
    RiskCard(radius = 24.dp, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(11.dp)).background(LemonBadge),
            ) {
                Text("!", style = EijyoTheme.typography.labelLarge.copy(fontSize = 13.sp), color = colors.lemonAccent)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                stringResource(R.string.risk_disclaimer),
                style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        }
    }
}
