package com.eijyo.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.RiskAssessment
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: Prediction)

    @Query("DELETE FROM prediction WHERE applicationId = :appId")
    suspend fun deleteByApplication(appId: String)

    @Query(
        "SELECT * FROM prediction WHERE applicationId = :appId " +
            "ORDER BY createdAt DESC LIMIT 1",
    )
    fun observeLatest(appId: String): Flow<Prediction?>
}

@Dao
interface RiskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: RiskAssessment)

    @Query("DELETE FROM risk_assessment WHERE applicationId = :appId")
    suspend fun deleteByApplication(appId: String)

    @Query(
        "SELECT * FROM risk_assessment WHERE applicationId = :appId " +
            "ORDER BY createdAt DESC LIMIT 1",
    )
    fun observeLatest(appId: String): Flow<RiskAssessment?>
}
