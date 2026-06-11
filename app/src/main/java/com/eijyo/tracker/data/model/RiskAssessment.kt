package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Result of the rule-based risk self-check. [score] is the raw rule total;
 * [level] is its bucketed level. [factors] lists the human-readable reasons that
 * contributed, shown in detail on the risk screen.
 */
@Entity(tableName = "risk_assessment")
data class RiskAssessment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String = ApplicationProfile.SINGLETON_ID,
    val level: RiskLevel,
    val score: Int,
    val factors: List<String>,
    val suggestions: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
)
