package com.eijyo.tracker.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.OnboardingRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides the app's start destination: returning users with a completed onboarding
 * go straight to [Routes.MAIN]; first-time users see [Routes.WELCOME]. Emits null
 * while the flag is still loading so the host can show a blank splash briefly.
 *
 * Also refreshes the prediction once on launch so the FIFO windows track the current
 * date + freshest public data, rather than staying frozen at the last profile edit.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val analysisRepository: AnalysisRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            profileRepository.getApplication()?.let { analysisRepository.refreshPrediction(it) }
        }
    }

    val startRoute: StateFlow<String?> = onboardingRepository.onboardingCompleted
        .map { done -> if (done) Routes.MAIN else Routes.WELCOME }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
