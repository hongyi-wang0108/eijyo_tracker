package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Output of the rule-based [com.eijyo.tracker.domain.prediction.PredictionEngine].
 * The three ranges are stored as display strings; [reasons] explains the factors
 * (kept as a list, serialized by a Room TypeConverter) so the prediction is never a
 * black box.
 */
@Entity(tableName = "prediction")
data class Prediction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String = ApplicationProfile.SINGLETON_ID,
    val optimisticRange: String,
    val normalRange: String,
    val conservativeRange: String,
    val confidenceLevel: ConfidenceLevel,
    val currentWaitDays: Int,
    val progressPercent: Int,
    val reasons: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
)
