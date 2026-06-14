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

/** Recent-window size for the μ_fast / μ_slow / fallback-average calculations. */
private const val RECENT_WINDOW = 6

/**
 * FIFO queue drainage — pure function with injectable [today] for unit tests.
 *
 * [series]       monthly data (pure-office caliber) in ascending month order; must be non-empty
 * [submitMonth]  user's submission month "YYYY-MM"
 * [today]        reference date (default: LocalDate.now())
 *
 * Algorithm (§6 of PREDICTION_AND_DATA.md):
 *   anchor    = submitMonth, clamped into [series.first, series.last]
 *   Q0        = pending at anchor month (end-of-month backlog ahead of you)
 *   ΣP        = Σ processed, strictly after the anchor month, up to series.last()
 *   R_latest  = Q0 − ΣP
 *   gap       = whole months between series.last().month and today (data-lag extrapolation)
 *   μ_latest  = processed[series.last()]  (fallback: average of last N months when 0)
 *   R_now     = R_latest − μ_latest × gap
 *   μ_fast    = max processed in last N months
 *   μ_slow    = min positive processed in last N months
 */
fun computeWait(
    series: List<MonthlyPoint>,
    submitMonth: String,
    today: LocalDate = LocalDate.now(),
): WaitResult {
    require(series.isNotEmpty()) { "series must be non-empty" }
    val sorted = series.sortedBy { it.month }
    val earliest = sorted.first()
    val latest = sorted.last()

    // 1. Anchor the queue position. Submit earlier than the data → anchor at the earliest
    //    month (conservative); otherwise the latest month at or before submitMonth.
    val anchor = if (submitMonth <= earliest.month) {
        earliest
    } else {
        sorted.lastOrNull { it.month <= submitMonth } ?: earliest
    }
    val q0 = anchor.pending

    // 2. Subtract everything processed *after* the anchor snapshot.
    val sigmaP = sorted.filter { it.month > anchor.month }.sumOf { it.processed }
    val rLatest = q0 - sigmaP

    // 3. Extrapolate the data-lag gap (latest data month → today) at the central speed.
    val gap = monthsBetween(latest.month, today).coerceAtLeast(0)

    // μ_central = average processed over the recent window. Using the average (not the
    // single latest month) keeps the central estimate stable and the optimistic/normal/
    // conservative band roughly symmetric — one anomalously slow/fast latest month no
    // longer skews "正常" toward one edge.
    val recent = sorted.takeLast(RECENT_WINDOW).map { it.processed }
    val muCentral = (if (recent.isNotEmpty()) recent.average() else 0.0).coerceAtLeast(1.0)

    val rNowExact = rLatest - muCentral * gap

    // 4. Speed bounds for the optimistic / conservative ends.
    val muFast = (recent.maxOrNull() ?: latest.processed).toDouble().coerceAtLeast(1.0)
    val muSlow = (recent.filter { it > 0 }.minOrNull()?.toDouble() ?: muCentral).coerceAtLeast(1.0)

    val state = if (rNowExact <= 0.0) WaitState.ALREADY_DUE else WaitState.WAITING

    return WaitResult(
        rNow = Math.round(rNowExact).toInt(),
        normalMonths = rNowExact / muCentral,
        optimisticMonths = rNowExact / muFast,
        conservativeMonths = rNowExact / muSlow,
        state = state,
    )
}

/** Whole months from a "YYYY-MM" anchor to [today] (ignores day-of-month). */
private fun monthsBetween(fromMonth: String, today: LocalDate): Int {
    val parts = fromMonth.split("-")
    if (parts.size != 2) return 0
    val year = parts[0].toIntOrNull() ?: return 0
    val month = parts[1].toIntOrNull() ?: return 0
    return (today.year - year) * 12 + (today.monthValue - month)
}
