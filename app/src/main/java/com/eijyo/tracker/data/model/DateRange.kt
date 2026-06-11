package com.eijyo.tracker.data.model

/**
 * A predicted result window expressed as human-readable boundary labels, e.g.
 * "2026年9月中旬" – "2026年11月上旬". Per the PM doc, predictions are only ever
 * shown as ranges, never as a single exact date.
 */
data class DateRange(
    val startLabel: String,
    val endLabel: String,
) {
    val display: String get() = "$startLabel - $endLabel"
}
