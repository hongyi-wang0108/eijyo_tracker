package com.eijyo.tracker.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.core.ui.component.WheelDate
import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import com.eijyo.tracker.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class OnboardingUiState(
    val answers: OnboardingAnswers = OnboardingAnswers(),
    val steps: List<OnboardingStep> = stepsFor(OnboardingAnswers()),
    val currentStep: OnboardingStep = OnboardingStep.NICKNAME,
    val isSaving: Boolean = false,
) {
    val stepIndex: Int get() = steps.indexOf(currentStep).coerceAtLeast(0)
    val totalSteps: Int get() = steps.size
    val progress: Float get() = (stepIndex + 1).toFloat() / totalSteps
    val progressLabel: String get() = "${stepIndex + 1} / $totalSteps"
    val isLastStep: Boolean get() = stepIndex == steps.lastIndex
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val draftJson = onboardingRepository.draft.first()
            if (!draftJson.isNullOrBlank()) {
                runCatching { Json.decodeFromString<OnboardingAnswers>(draftJson) }
                    .getOrNull()
                    ?.let { restored ->
                        _state.update { it.copy(answers = restored, steps = stepsFor(restored)) }
                    }
            }
        }
    }

    // ---- Answer updates -------------------------------------------------------

    fun updateNickname(value: String) = mutate { it.copy(nickname = value) }
    fun selectVisa(value: VisaType) = mutate { it.copy(visaType = value) }
    fun selectStatus(value: ApplicationStatus) = mutate { it.copy(submittedStatus = value) }
    fun selectOffice(value: ImmigrationOffice) = mutate { it.copy(office = value) }
    fun selectPath(value: ApplicationPath) = mutate { it.copy(path = value) }
    fun selectTax(value: TriState) = mutate { it.copy(tax = value) }
    fun selectPension(value: TriState) = mutate { it.copy(pension = value) }
    fun selectHealth(value: TriState) = mutate { it.copy(health = value) }
    fun selectIncome(value: IncomeRange) = mutate { it.copy(income = value) }
    fun setDependents(value: Int) = mutate { it.copy(dependents = value.coerceIn(0, 20)) }
    fun selectSupplement(value: TriState) = mutate { it.copy(supplement = value) }

    fun setDate(date: WheelDate) = mutate {
        it.copy(year = date.year, month = date.month, day = date.day)
    }

    fun setDateMode(mode: DatePrecision) = mutate {
        // Switching to "month / later" relaxes the day component.
        if (mode == DatePrecision.MONTH) it.copy(dateMode = mode, day = null) else it.copy(dateMode = mode)
    }

    private fun mutate(transform: (OnboardingAnswers) -> OnboardingAnswers) {
        _state.update { current ->
            val updated = transform(current.answers)
            current.copy(answers = updated, steps = stepsFor(updated))
        }
        saveDraft()
    }

    private fun saveDraft() {
        val answers = _state.value.answers
        viewModelScope.launch {
            onboardingRepository.saveDraft(Json.encodeToString(answers))
        }
    }

    // ---- Navigation -----------------------------------------------------------

    /** Whether the current step has a valid answer and the user may advance. */
    fun canProceed(): Boolean {
        val a = _state.value.answers
        return when (_state.value.currentStep) {
            OnboardingStep.NICKNAME -> a.nickname.isNotBlank()
            OnboardingStep.VISA -> a.visaType != null
            OnboardingStep.SUBMITTED_STATUS -> a.submittedStatus != null
            OnboardingStep.APP_DATE -> a.dateMode == DatePrecision.UNKNOWN ||
                (a.year != null && a.month != null)
            OnboardingStep.OFFICE -> a.office != null
            OnboardingStep.PATH -> a.path != null
            OnboardingStep.TAX -> a.tax != null
            OnboardingStep.PENSION -> a.pension != null
            OnboardingStep.HEALTH -> a.health != null
            OnboardingStep.INCOME -> true
            OnboardingStep.DEPENDENTS -> true
            OnboardingStep.SUPPLEMENT -> a.supplement != null
        }
    }

    /** Advances to the next step, or finishes if on the last one. */
    fun next(onFinished: () -> Unit) {
        val s = _state.value
        val idx = s.steps.indexOf(s.currentStep)
        if (idx < s.steps.lastIndex) {
            _state.update { it.copy(currentStep = it.steps[idx + 1]) }
        } else {
            finish(onFinished)
        }
    }

    /** Goes back a step, or exits onboarding when already at the first step. */
    fun back(onExit: () -> Unit) {
        val s = _state.value
        val idx = s.steps.indexOf(s.currentStep)
        if (idx > 0) {
            _state.update { it.copy(currentStep = it.steps[idx - 1]) }
        } else {
            onExit()
        }
    }

    private fun finish(onFinished: () -> Unit) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val answers = _state.value.answers
            onboardingRepository.complete(answers.toUserProfile(), answers.toApplicationProfile())
            _state.update { it.copy(isSaving = false) }
            onFinished()
        }
    }
}

/** Builds the visible step list; date/office/supplement only matter once submitted. */
private fun stepsFor(answers: OnboardingAnswers): List<OnboardingStep> = buildList {
    add(OnboardingStep.NICKNAME)
    add(OnboardingStep.VISA)
    add(OnboardingStep.SUBMITTED_STATUS)
    if (answers.hasSubmitted) {
        add(OnboardingStep.APP_DATE)
        add(OnboardingStep.OFFICE)
    }
    add(OnboardingStep.PATH)
    add(OnboardingStep.TAX)
    add(OnboardingStep.PENSION)
    add(OnboardingStep.HEALTH)
    add(OnboardingStep.INCOME)
    add(OnboardingStep.DEPENDENTS)
    if (answers.hasSubmitted) {
        add(OnboardingStep.SUPPLEMENT)
    }
}
