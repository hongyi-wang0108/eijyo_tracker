package com.eijyo.tracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Top-level document from public-data.json (A2 static JSON, see docs/PREDICTION_AND_DATA.md). */
@Serializable
data class PublicDataDoc(
    val schemaVersion: Int,
    val dataAsOf: String,
    val updatedAt: String,
    val source: DataSource,
    val standardProcessing: StandardProcessing,
    val offices: List<OfficeData>,
) {
    fun officeData(code: String): OfficeData? = offices.firstOrNull { it.code == code }
}

@Serializable
data class DataSource(
    val name: String,
    val mainTableUrl: String,
    val permitTableUrl: String,
)

@Serializable
data class StandardProcessing(
    val rangeLabel: String,
    val minMonths: Int,
    val maxMonths: Int,
)

@Serializable
data class OfficeData(
    val code: String,
    val displayName: String,
    val regionalNote: String,
    val monthly: List<MonthlyPoint>,
    val bureauTotal: BureauTotal? = null,
    val permitsByYear: List<YearlyPermit>? = null,
    /**
     * Multiplier applied to the FIFO wait time so the prediction matches real observed
     * latency, which runs longer than the naive backlog÷throughput estimate (the queue
     * isn't strictly FIFO; resources are shared across statuses; some cases stall).
     * 1.0 = no calibration. Tuned per office from ground truth (e.g. Tokyo ≈ 700 days).
     */
    val calibrationFactor: Double = 1.0,
)

/**
 * One calendar month of reception/processing stats.
 * [month] is "YYYY-MM"; values are pure-office (branches subtracted) unless this
 * appears inside [BureauTotal.monthly], which holds raw 管内合計.
 *
 * - [received] = 受理_新受(103000): new arrivals this month (λ)
 * - [processed] = 既済_総数(300000): cases resolved this month (μ)
 * - [pending]   = 受理_総数(100000) − 既済_総数: end-of-month backlog (Q)
 */
@Serializable
data class MonthlyPoint(
    val month: String,
    val received: Int,
    val processed: Int,
    val pending: Int,
)

/** Bureau-level totals (管内合計), shown as a comparison baseline in the Data tab. */
@Serializable
data class BureauTotal(
    val label: String,
    val monthly: List<MonthlyPoint>,
)

@Serializable
data class YearlyPermit(
    val year: Int,
    val count: Int,
)
