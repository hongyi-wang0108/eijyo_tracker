package com.eijyo.tracker.domain.prediction

import com.eijyo.tracker.data.model.MonthlyPoint
import java.time.LocalDate

/** Outcome of the FIFO queue wait computation. */
enum class WaitState {
    /** Still in queue; [WaitResult.normalMonths] etc. give the expected wait. */
    WAITING,
    /** R_now ≤ 0: statistically past your turn — result may arrive soon. */
    ALREADY_DUE,
}

/**
 * Intermediate, numeric result of [computeWait].
 *断言数值中间量 (not formatted strings) so tests stay stable across label changes.
 *
 * [rNow]               remaining people ahead as of today (can be ≤ 0 → ALREADY_DUE)
 * [normalMonths]       R_now / μ_latest
 * [optimisticMonths]   R_now / μ_fast  (max processed in recent window)
 * [conservativeMonths] R_now / μ_slow  (min processed in recent window)
 */
data class WaitResult(
    val rNow: Int,
    val normalMonths: Double,
    val optimisticMonths: Double,
    val conservativeMonths: Double,
    val state: WaitState,
)

/**
 * FIFO queue drainage — pure function with injectable [today] for unit tests.
 *
 * [series]       monthly data (pure-office caliber) in ascending month order; must be non-empty
 * [submitMonth]  user's submission month "YYYY-MM"
 * [today]        reference date (default: LocalDate.now())
 *
 * Algorithm (§6 of PREDICTION_AND_DATA.md):
 *   Q0        = pending[submitMonth] (if submitMonth < series start → use earliest pending)
 *   ΣP        = Σ processed, from month after submitMonth up to series.last()
 *   R_latest  = Q0 − ΣP
 *   gap       = whole months between series.last().month and today
 *   μ_latest  = processed[series.last()]  (fallback: average of last N months if 0)
 *   R_now     = R_latest − μ_latest × gap
 *   μ_fast    = max processed in last N months
 *   μ_slow    = min processed in last N months (> 0)
 *
 * Step 5 implements this; the stub allows TDD tests to compile and run red.
 */
fun computeWait(
    series: List<MonthlyPoint>,
    submitMonth: String,
    today: LocalDate = LocalDate.now(),
): WaitResult = TODO("Step 5: implement FIFO algorithm — see §6 of docs/PREDICTION_AND_DATA.md")
