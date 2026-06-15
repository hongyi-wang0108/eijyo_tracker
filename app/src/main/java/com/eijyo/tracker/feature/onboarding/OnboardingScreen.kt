package com.eijyo.tracker.feature.onboarding

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.R
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
                    text = stringResource(titleFor(state.currentStep)),
                    style = EijyoTheme.typography.headlineMedium.copy(fontSize = 27.sp),
                    color = colors.ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(hintFor(state.currentStep)),
                    style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = colors.inkMuted,
                )

                Spacer(Modifier.height(20.dp))
                QuestionContent(state = state, viewModel = viewModel)
            }

            BottomBar(
                nextLabel = stringResource(if (state.isLastStep) R.string.onboarding_finish else R.string.onboarding_next),
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
                contentDescription = stringResource(R.string.onboarding_back_cd),
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
                text = stringResource(R.string.onboarding_helper_title),
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_helper_subtitle),
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
            Text(stringResource(R.string.onboarding_prev), style = EijyoTheme.typography.labelLarge, color = colors.inkMuted)
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
            options = VisaType.entries.map { it to stringResource(it.labelRes) },
            selected = a.visaType,
            onSelect = viewModel::selectVisa,
        )

        OnboardingStep.SUBMITTED_STATUS -> ChoiceColumn(
            options = listOf(
                ApplicationStatus.PREPARING to stringResource(R.string.onboarding_status_preparing),
                ApplicationStatus.REVIEWING to stringResource(R.string.onboarding_status_reviewing),
                ApplicationStatus.COMPLETED to stringResource(R.string.onboarding_status_completed),
            ),
            selected = a.submittedStatus,
            onSelect = viewModel::selectStatus,
        )

        OnboardingStep.APP_DATE -> DateQuestion(state, viewModel)

        OnboardingStep.OFFICE -> ChoiceColumn(
            options = ImmigrationOffice.entries.map { it to stringResource(it.labelRes) },
            selected = a.office,
            onSelect = viewModel::selectOffice,
        )

        OnboardingStep.PATH -> ChoiceColumn(
            options = ApplicationPath.entries.map { it to stringResource(it.labelRes) },
            selected = a.path,
            onSelect = viewModel::selectPath,
        )

        OnboardingStep.TAX -> ChoiceColumn(
            options = TriState.entries.map { it to stringResource(it.labelRes) },
            selected = a.tax,
            onSelect = viewModel::selectTax,
        )

        OnboardingStep.PENSION -> ChoiceColumn(
            options = TriState.entries.map { it to stringResource(it.labelRes) },
            selected = a.pension,
            onSelect = viewModel::selectPension,
        )

        OnboardingStep.HEALTH -> ChoiceColumn(
            options = TriState.entries.map { it to stringResource(it.labelRes) },
            selected = a.health,
            onSelect = viewModel::selectHealth,
        )

        OnboardingStep.INCOME -> ChoiceColumn(
            options = IncomeRange.entries.map { it to stringResource(it.labelRes) },
            selected = a.income,
            onSelect = viewModel::selectIncome,
        )

        OnboardingStep.DEPENDENTS -> DependentsStepper(a.dependents, viewModel::setDependents)

        OnboardingStep.SUPPLEMENT -> ChoiceColumn(
            options = listOf(
                TriState.NO to stringResource(R.string.onboarding_supplement_no),
                TriState.YES to stringResource(R.string.onboarding_supplement_yes),
                TriState.UNKNOWN to stringResource(R.string.onboarding_supplement_unknown),
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
        placeholder = { Text(stringResource(R.string.onboarding_nickname_placeholder)) },
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
            DateModeChip(stringResource(R.string.onboarding_date_mode_day), a.dateMode == DatePrecision.DAY) {
                viewModel.setDateMode(DatePrecision.DAY)
            }
            DateModeChip(stringResource(R.string.onboarding_date_mode_month), a.dateMode == DatePrecision.MONTH) {
                viewModel.setDateMode(DatePrecision.MONTH)
            }
            DateModeChip(stringResource(R.string.onboarding_date_mode_unknown), a.dateMode == DatePrecision.UNKNOWN) {
                viewModel.setDateMode(DatePrecision.UNKNOWN)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (a.dateMode == DatePrecision.UNKNOWN) {
            Text(
                text = stringResource(R.string.onboarding_date_unknown_hint),
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
            text = stringResource(R.string.onboarding_dependents_count, count),
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

@StringRes
private fun titleFor(step: OnboardingStep): Int = when (step) {
    OnboardingStep.NICKNAME -> R.string.onboarding_title_nickname
    OnboardingStep.VISA -> R.string.onboarding_title_visa
    OnboardingStep.SUBMITTED_STATUS -> R.string.onboarding_title_status
    OnboardingStep.APP_DATE -> R.string.onboarding_title_date
    OnboardingStep.OFFICE -> R.string.onboarding_title_office
    OnboardingStep.PATH -> R.string.onboarding_title_path
    OnboardingStep.TAX -> R.string.onboarding_title_tax
    OnboardingStep.PENSION -> R.string.onboarding_title_pension
    OnboardingStep.HEALTH -> R.string.onboarding_title_health
    OnboardingStep.INCOME -> R.string.onboarding_title_income
    OnboardingStep.DEPENDENTS -> R.string.onboarding_title_dependents
    OnboardingStep.SUPPLEMENT -> R.string.onboarding_title_supplement
}

@StringRes
private fun hintFor(step: OnboardingStep): Int = when (step) {
    OnboardingStep.NICKNAME -> R.string.onboarding_hint_nickname
    OnboardingStep.VISA -> R.string.onboarding_hint_visa
    OnboardingStep.SUBMITTED_STATUS -> R.string.onboarding_hint_status
    OnboardingStep.APP_DATE -> R.string.onboarding_hint_date
    OnboardingStep.OFFICE -> R.string.onboarding_hint_office
    OnboardingStep.PATH -> R.string.onboarding_hint_path
    OnboardingStep.TAX -> R.string.onboarding_hint_tax
    OnboardingStep.PENSION -> R.string.onboarding_hint_pension
    OnboardingStep.HEALTH -> R.string.onboarding_hint_health
    OnboardingStep.INCOME -> R.string.onboarding_hint_income
    OnboardingStep.DEPENDENTS -> R.string.onboarding_hint_dependents
    OnboardingStep.SUPPLEMENT -> R.string.onboarding_hint_supplement
}
