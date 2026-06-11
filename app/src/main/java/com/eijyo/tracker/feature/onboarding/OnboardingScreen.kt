package com.eijyo.tracker.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.OnboardingBackground
import com.eijyo.tracker.core.ui.component.PawPrint
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import java.time.LocalDate

private val ShadowTint = Color(0x1A8C5C3D)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onExit: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EijyoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screen),
    ) {
        OnboardingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            TopBar(
                progress = state.progress,
                progressLabel = state.progressLabel,
                onBack = { viewModel.back(onExit) },
            )

            Spacer(Modifier.height(24.dp))
            HelperBubble()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 24.dp),
            ) {
                Text(
                    text = titleFor(state.currentStep),
                    style = EijyoTheme.typography.headlineMedium.copy(fontSize = 27.sp),
                    color = colors.ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = hintFor(state.currentStep),
                    style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = colors.inkMuted,
                )

                Spacer(Modifier.height(20.dp))
                QuestionContent(state = state, viewModel = viewModel)
            }

            BottomBar(
                nextLabel = if (state.isLastStep) "完成" else "继续",
                nextEnabled = viewModel.canProceed() && !state.isSaving,
                onPrev = { viewModel.back(onExit) },
                onNext = { viewModel.next(onFinished) },
            )
        }
    }
}

@Composable
private fun TopBar(progress: Float, progressLabel: String, onBack: () -> Unit) {
    val colors = EijyoTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .shadow(6.dp, RoundedCornerShape(19.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
                .clip(RoundedCornerShape(19.dp))
                .background(colors.card)
                .clickable(onClick = onBack),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "返回上一题",
                tint = colors.ink,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.border),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.mint),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = progressLabel,
            style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = colors.inkMuted,
        )
    }
}

/** Fixed "dog asks a question" helper card: a dog face plus a friendly two-line note. */
@Composable
private fun HelperBubble() {
    val colors = EijyoTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.card)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        DogFace(size = 48.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "小狗先问你一个问题",
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "选好后，我会帮你生成预测和材料清单",
                style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun BottomBar(
    nextLabel: String,
    nextEnabled: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = EijyoTheme.colors
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(112.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(colors.card)
                .border(1.dp, colors.border, RoundedCornerShape(25.dp))
                .clickable(onClick = onPrev),
        ) {
            Text("上一步", style = EijyoTheme.typography.labelLarge, color = colors.inkMuted)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .shadow(10.dp, RoundedCornerShape(25.dp), spotColor = Color(0x3D2E855C), ambientColor = Color(0x3D2E855C))
                .clip(RoundedCornerShape(25.dp))
                .background(if (nextEnabled) colors.mint else colors.mintContainer)
                .clickable(enabled = nextEnabled, onClick = onNext),
        ) {
            Text(
                text = nextLabel,
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = if (nextEnabled) Color.White else colors.inkMuted,
            )
            if (nextEnabled) {
                PawPrint(
                    color = Color.White,
                    size = 19.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val a = state.answers
    when (state.currentStep) {
        OnboardingStep.NICKNAME -> NicknameInput(a.nickname, viewModel::updateNickname)

        OnboardingStep.VISA -> ChoiceColumn(
            options = VisaType.entries.map { it to it.label },
            selected = a.visaType,
            onSelect = viewModel::selectVisa,
        )

        OnboardingStep.SUBMITTED_STATUS -> ChoiceColumn(
            options = listOf(
                ApplicationStatus.PREPARING to "还没，正在准备",
                ApplicationStatus.REVIEWING to "已经提交",
                ApplicationStatus.COMPLETED to "已经收到结果",
            ),
            selected = a.submittedStatus,
            onSelect = viewModel::selectStatus,
        )

        OnboardingStep.APP_DATE -> DateQuestion(state, viewModel)

        OnboardingStep.OFFICE -> ChoiceColumn(
            options = ImmigrationOffice.entries.map { it to it.label },
            selected = a.office,
            onSelect = viewModel::selectOffice,
        )

        OnboardingStep.PATH -> ChoiceColumn(
            options = ApplicationPath.entries.map { it to it.label },
            selected = a.path,
            onSelect = viewModel::selectPath,
        )

        OnboardingStep.TAX -> ChoiceColumn(
            options = TriState.entries.map { it to it.label },
            selected = a.tax,
            onSelect = viewModel::selectTax,
        )

        OnboardingStep.PENSION -> ChoiceColumn(
            options = TriState.entries.map { it to it.label },
            selected = a.pension,
            onSelect = viewModel::selectPension,
        )

        OnboardingStep.HEALTH -> ChoiceColumn(
            options = TriState.entries.map { it to it.label },
            selected = a.health,
            onSelect = viewModel::selectHealth,
        )

        OnboardingStep.INCOME -> ChoiceColumn(
            options = IncomeRange.entries.map { it to it.label },
            selected = a.income,
            onSelect = viewModel::selectIncome,
        )

        OnboardingStep.DEPENDENTS -> DependentsStepper(a.dependents, viewModel::setDependents)

        OnboardingStep.SUPPLEMENT -> ChoiceColumn(
            options = listOf(
                TriState.NO to "没有",
                TriState.YES to "收到过",
                TriState.UNKNOWN to "不确定",
            ),
            selected = a.supplement,
            onSelect = viewModel::selectSupplement,
        )
    }
}

@Composable
private fun NicknameInput(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text("例如：小王 / Yuki") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun DateQuestion(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    val colors = EijyoTheme.colors
    val a = state.answers
    val today = remember { LocalDate.now() }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateModeChip("精确到日", a.dateMode == DatePrecision.DAY) {
                viewModel.setDateMode(DatePrecision.DAY)
            }
            DateModeChip("只记得月份", a.dateMode == DatePrecision.MONTH) {
                viewModel.setDateMode(DatePrecision.MONTH)
            }
            DateModeChip("暂不确定", a.dateMode == DatePrecision.UNKNOWN) {
                viewModel.setDateMode(DatePrecision.UNKNOWN)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (a.dateMode == DatePrecision.UNKNOWN) {
            Text(
                text = "可以稍后在「申请」页补充具体日期。日期填写后才能生成预计时间。",
                style = EijyoTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
        } else {
            com.eijyo.tracker.core.ui.component.WheelDatePicker(
                value = com.eijyo.tracker.core.ui.component.WheelDate(
                    year = a.year ?: today.year,
                    month = a.month ?: today.monthValue,
                    day = a.day ?: today.dayOfMonth,
                ),
                monthOnly = a.dateMode == DatePrecision.MONTH,
                onValueChange = viewModel::setDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }
    }
}

@Composable
private fun DateModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.mintWash else colors.card)
            .border(
                width = 1.dp,
                color = if (selected) colors.mint else colors.border,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            style = EijyoTheme.typography.labelMedium,
            color = if (selected) colors.ink else colors.inkMuted,
        )
    }
}

@Composable
private fun DependentsStepper(count: Int, onChange: (Int) -> Unit) {
    val colors = EijyoTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        StepperButton("－") { onChange(count - 1) }
        Text(
            text = "$count 人",
            style = EijyoTheme.typography.headlineMedium,
            color = colors.ink,
        )
        StepperButton("＋") { onChange(count + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.mintContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, style = EijyoTheme.typography.titleLarge, color = colors.ink)
    }
}

@Composable
private fun <T> ChoiceColumn(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (value, label) ->
            ChoiceRow(label = label, selected = value == selected) { onSelect(value) }
        }
    }
}

/** A single-select option row: a leading dot (filled + ✓ when selected) and a label. */
@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .shadow(if (selected) 8.dp else 4.dp, RoundedCornerShape(23.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(23.dp))
            .background(if (selected) colors.mintWash else colors.card)
            .then(
                if (selected) Modifier.border(1.4.dp, colors.mint, RoundedCornerShape(23.dp)) else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (selected) colors.mint else colors.border),
        ) {
            if (selected) {
                Text("✓", style = EijyoTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color.White)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = colors.ink,
        )
    }
}

private fun titleFor(step: OnboardingStep): String = when (step) {
    OnboardingStep.NICKNAME -> "我们怎么称呼你？"
    OnboardingStep.VISA -> "你现在的在留资格是？"
    OnboardingStep.SUBMITTED_STATUS -> "你申请永住了吗？"
    OnboardingStep.APP_DATE -> "你的永住申请是哪天提交的？"
    OnboardingStep.OFFICE -> "你提交到哪个入管？"
    OnboardingStep.PATH -> "你按哪种路径申请？"
    OnboardingStep.TAX -> "最近几年住民税是否按时缴纳？"
    OnboardingStep.PENSION -> "年金是否连续缴纳？"
    OnboardingStep.HEALTH -> "健康保险是否连续缴纳？"
    OnboardingStep.INCOME -> "你的年收范围？"
    OnboardingStep.DEPENDENTS -> "你有几位扶养人？"
    OnboardingStep.SUPPLEMENT -> "是否收到过补资料通知？"
}

private fun hintFor(step: OnboardingStep): String = when (step) {
    OnboardingStep.NICKNAME -> "我会用这个称呼你，首页问候也会用到～"
    OnboardingStep.VISA -> "这个问题会影响预测和材料清单"
    OnboardingStep.SUBMITTED_STATUS -> "告诉我进度，我才知道该陪你做什么。"
    OnboardingStep.APP_DATE -> "记得大概就行，不确定也没关系。"
    OnboardingStep.OFFICE -> "不同入管处理速度不太一样。"
    OnboardingStep.PATH -> "不确定的话先选「不确定」也可以。"
    OnboardingStep.TAX -> "纳税记录是永住审查的重点之一。"
    OnboardingStep.PENSION -> "年金连续缴纳很重要，如实填写就好。"
    OnboardingStep.HEALTH -> "健康保险也会被一起确认。"
    OnboardingStep.INCOME -> "用于判断生计稳定性，不想填可以跳过。"
    OnboardingStep.DEPENDENTS -> "扶养人数也会影响生计判断。"
    OnboardingStep.SUPPLEMENT -> "收到过补资料的话，我帮你记进时间线。"
}
