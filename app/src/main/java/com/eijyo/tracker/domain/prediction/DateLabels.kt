package com.eijyo.tracker.domain.prediction

import com.eijyo.tracker.data.model.DatePrecision
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Helpers for turning dates into the "上旬/中旬/下旬" (early/mid/late month) labels the
 * design uses, and for parsing the precision-tagged submitted date string.
 */
object DateLabels {

    private val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /** "2026年9月中旬" — the third (旬) of the month is derived from the day-of-month. */
    fun monthThird(date: LocalDate): String {
        val third = when {
            date.dayOfMonth <= 10 -> "上旬"
            date.dayOfMonth <= 20 -> "中旬"
            else -> "下旬"
        }
        return "${date.year}年${date.monthValue}月$third"
    }

    /** "2026年9月" */
    fun yearMonth(date: LocalDate): String = "${date.year}年${date.monthValue}月"

    /**
     * Resolves the stored [submittedDate] string to an anchor [LocalDate] for math.
     * DAY → the exact date; MONTH → the 15th (mid-month) as an estimate; UNKNOWN → null.
     */
    fun resolveSubmitted(submittedDate: String?, precision: DatePrecision): LocalDate? {
        if (submittedDate.isNullOrBlank()) return null
        return runCatching {
            when (precision) {
                DatePrecision.DAY -> LocalDate.parse(submittedDate, dayFormat)
                DatePrecision.MONTH -> {
                    val ym = LocalDate.parse("$submittedDate-01", dayFormat)
                    ym.withDayOfMonth(15)
                }
                DatePrecision.UNKNOWN -> null
            }
        }.getOrNull()
    }

    fun format(date: LocalDate): String = date.format(dayFormat)
    fun formatMonth(date: LocalDate): String = date.format(monthFormat)
}
