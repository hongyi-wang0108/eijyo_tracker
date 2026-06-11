package com.eijyo.tracker.core.ui.component

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.time.LocalDate
import java.time.YearMonth

/**
 * Selected date from the wheel picker. [day] is null when the user only picks a
 * year/month (the "我不确定具体日期" mode from PM doc Q4).
 */
data class WheelDate(val year: Int, val month: Int, val day: Int?)

/**
 * Native Android scroll-wheel date picker built from [NumberPicker]s, as the PM doc
 * explicitly requires the platform wheel control. When [monthOnly] is true the day
 * wheel is hidden and the result has a null day (MONTH precision).
 */
@Composable
fun WheelDatePicker(
    value: WheelDate,
    monthOnly: Boolean,
    onValueChange: (WheelDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentValue = rememberUpdatedState(value)
    val callback = rememberUpdatedState(onValueChange)
    val thisYear = remember { LocalDate.now().year }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            fun pickerParams() = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            // Use displayedValues instead of setFormatter: NumberPicker's formatter is
            // not applied to the *currently selected* (center) value until the wheel is
            // scrolled, so the center item would show a bare number with no 年/月/日 unit.
            val yearPicker = NumberPicker(context).apply {
                minValue = thisYear - 15
                maxValue = thisYear
                wrapSelectorWheel = false
                displayedValues = Array(maxValue - minValue + 1) { "${minValue + it}年" }
            }
            val monthPicker = NumberPicker(context).apply {
                minValue = 1
                maxValue = 12
                wrapSelectorWheel = true
                displayedValues = Array(12) { "${it + 1}月" }
            }
            // Day labels are sized for the longest month (31). The visible range is
            // narrowed via maxValue in syncDayRange; the array stays full-length so it
            // always covers the active range.
            val dayPicker = NumberPicker(context).apply {
                minValue = 1
                maxValue = 31
                wrapSelectorWheel = true
                displayedValues = Array(31) { "${it + 1}日" }
            }

            fun emit() {
                val day = if (dayPicker.visibility == View.GONE) null else dayPicker.value
                callback.value(WheelDate(yearPicker.value, monthPicker.value, day))
            }

            fun syncDayRange() {
                val max = YearMonth.of(yearPicker.value, monthPicker.value).lengthOfMonth()
                dayPicker.maxValue = max
                if (dayPicker.value > max) dayPicker.value = max
            }

            yearPicker.setOnValueChangedListener { _, _, _ -> syncDayRange(); emit() }
            monthPicker.setOnValueChangedListener { _, _, _ -> syncDayRange(); emit() }
            dayPicker.setOnValueChangedListener { _, _, _ -> emit() }

            container.addView(yearPicker, pickerParams())
            container.addView(monthPicker, pickerParams())
            container.addView(dayPicker, pickerParams())
            container.tag = Triple(yearPicker, monthPicker, dayPicker)
            container
        },
        update = { container ->
            @Suppress("UNCHECKED_CAST")
            val pickers = container.tag as Triple<NumberPicker, NumberPicker, NumberPicker>
            val (yearPicker, monthPicker, dayPicker) = pickers
            val v = currentValue.value
            yearPicker.value = v.year
            monthPicker.value = v.month
            dayPicker.maxValue = YearMonth.of(v.year, v.month).lengthOfMonth()
            dayPicker.value = (v.day ?: dayPicker.value).coerceIn(1, dayPicker.maxValue)
            dayPicker.visibility = if (monthOnly) View.GONE else View.VISIBLE
        },
    )
}
