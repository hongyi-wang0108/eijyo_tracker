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
) {
    fun observePrediction(appId: String = ApplicationProfile.SINGLETON_ID): Flow<Prediction?> =
        predictionDao.observeLatest(appId)

    fun observeRisk(appId: String = ApplicationProfile.SINGLETON_ID): Flow<RiskAssessment?> =
        riskDao.observeLatest(appId)

    suspend fun regenerate(profile: ApplicationProfile) {
        riskDao.deleteByApplication(profile.id)
        riskDao.insert(riskEngine.assess(profile))

        predictionDao.deleteByApplication(profile.id)
        val publicData = PublicDataSource.forOffice(profile.submittedOffice)
        predictionEngine.predict(profile, publicData)?.let { predictionDao.insert(it) }
    }
}
