package com.eijyo.tracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "eijyo_prefs")

/**
 * Lightweight key-value state that doesn't belong in Room: the first-launch /
 * onboarding-completed flag, and the serialized onboarding draft so a half-finished
 * questionnaire can be restored after the user leaves.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val ONBOARDING_DRAFT = stringPreferencesKey("onboarding_draft")
    }

    val onboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val onboardingDraft: Flow<String?> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DRAFT] }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun saveOnboardingDraft(json: String) {
        context.dataStore.edit { it[Keys.ONBOARDING_DRAFT] = json }
    }

    suspend fun clearOnboardingDraft() {
        context.dataStore.edit { it.remove(Keys.ONBOARDING_DRAFT) }
    }
}
