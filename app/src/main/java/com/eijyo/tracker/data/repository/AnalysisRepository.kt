package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.dao.PredictionDao
import com.eijyo.tracker.data.local.dao.RiskDao
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.staticdata.PublicDataSource
import com.eijyo.tracker.domain.prediction.PredictionEngine
import com.eijyo.tracker.domain.risk.RiskEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the derived analysis artifacts — the risk assessment and the prediction —
 * recomputing them from the profile via the pure rule engines and persisting the
 * latest result.
 */
@Singleton
class AnalysisRepository @Inject constructor(
    private val predictionDao: PredictionDao,
    private val riskDao: RiskDao,
    private val predictionEngine: PredictionEngine,
    private val riskEngine: RiskEngine,
    private val publicDataRepository: PublicDataRepository,
) {
    fun observePrediction(appId: String = ApplicationProfile.SINGLETON_ID): Flow<Prediction?> =
        predictionDao.observeLatest(appId)

    fun observeRisk(appId: String = ApplicationProfile.SINGLETON_ID): Flow<RiskAssessment?> =
        riskDao.observeLatest(appId)

    suspend fun regenerate(profile: ApplicationProfile) {
        riskDao.deleteByApplication(profile.id)
        riskDao.insert(riskEngine.assess(profile))
        refreshPrediction(profile)
    }

    /**
     * Recomputes only the prediction (the date-sensitive artifact). Call on app launch so
     * the FIFO windows reflect the current date + the freshest public data, instead of being
     * frozen at the last profile edit. Risk doesn't depend on the date, so it's left alone.
     */
    suspend fun refreshPrediction(profile: ApplicationProfile) {
        predictionDao.deleteByApplication(profile.id)
        // Prefer the FIFO model on real e-Stat data (three-tier fallback, never throws).
        // If even the bundled asset can't be read, fall back to the static standard period.
        val prediction = runCatching { publicDataRepository.load().doc }
            .getOrNull()
            ?.let { doc -> predictionEngine.predict(profile, doc) }
            ?: predictionEngine.predict(profile, PublicDataSource.forOffice(profile.submittedOffice))
        prediction?.let { predictionDao.insert(it) }
    }
}
