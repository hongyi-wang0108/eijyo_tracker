package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.AppPreferences
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the onboarding lifecycle: whether it's done, the in-progress draft,
 * and the final commit that turns answers into a saved profile plus its generated
 * documents, risk assessment and prediction (PM doc 6.3.7).
 */
@Singleton
class OnboardingRepository @Inject constructor(
    private val preferences: AppPreferences,
    private val profileRepository: ProfileRepository,
    private val documentRepository: DocumentRepository,
    private val analysisRepository: AnalysisRepository,
) {
    val onboardingCompleted: Flow<Boolean> = preferences.onboardingCompleted
    val draft: Flow<String?> = preferences.onboardingDraft

    suspend fun saveDraft(json: String) = preferences.saveOnboardingDraft(json)

    /**
     * Persists the finished profile and rebuilds every derived artifact, then marks
     * onboarding complete and clears the draft. Ordering matches PM doc 6.3.7.
     */
    suspend fun complete(user: UserProfile, application: ApplicationProfile) {
        profileRepository.saveUser(user)
        profileRepository.saveApplication(application)
        documentRepository.regenerate(application)
        analysisRepository.regenerate(application)
        preferences.clearOnboardingDraft()
        preferences.setOnboardingCompleted(true)
    }
}
