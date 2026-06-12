package com.eijyo.tracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.local.LanguagePrefs
import com.eijyo.tracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String = "",
    val statusLabel: String = "",
    val officeLabel: String = "",
    val currentLanguage: String = "zh",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val state = combine(
        profileRepo.observeUser(),
        profileRepo.observeApplication(),
    ) { user, app ->
        SettingsUiState(
            displayName = user?.nickname?.takeIf { it.isNotBlank() } ?: "",
            statusLabel = app?.status?.label ?: "",
            officeLabel = app?.submittedOffice?.label ?: "",
            currentLanguage = LanguagePrefs.get(appContext),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(currentLanguage = LanguagePrefs.get(appContext)),
    )

    fun saveLanguage(code: String) {
        LanguagePrefs.set(appContext, code)
    }
}
