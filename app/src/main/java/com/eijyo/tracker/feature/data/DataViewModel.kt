package com.eijyo.tracker.feature.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.R
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.OfficeData
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.PublicDataOrigin
import com.eijyo.tracker.data.repository.PublicDataRepository
import com.eijyo.tracker.data.repository.PublicDataResult
import com.eijyo.tracker.data.staticdata.PublicDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Latest-month reception/processing stats for the user's office (pure-office caliber). */
data class OfficeStats(
    val latestMonthLabel: String,
    val received: Int,
    val processed: Int,
    val pending: Int,
    /** pending ÷ processed — months to clear the current backlog at current speed. */
    val queueMonths: Double?,
    /** Bureau-total comparison (管内合计), null when the office has no sub-branches. */
    val bureauLabel: String? = null,
    val bureauPending: Int? = null,
)

/** One bar in the trend chart. */
data class TrendBar(val label: String, val value: Int)

data class DataUiState(
    val officeLabel: String = "",
    val visaTypeLabel: String = "",
    val pathLabel: String = "",
    val loading: Boolean = true,
    val standardRange: String = "4 - 6 个月",
    val standardMinMonths: Int = 4,
    val standardMaxMonths: Int = 6,
    val sourceName: String = "出入国在留管理庁",
    val dataAsOfLabel: String = "",
    val originLabel: String = "",
    val regionalNote: String = "",
    val stats: OfficeStats? = null,
    val trendTitle: String = "",
    val trendUnit: String = "",
    val trend: List<TrendBar> = emptyList(),
)

@HiltViewModel
class DataViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    profileRepo: ProfileRepository,
    private val publicDataRepo: PublicDataRepository,
) : ViewModel() {

    init {
        // Kick off the fallback chain; publicDataRepo.state emits when resolved.
        viewModelScope.launch { runCatching { publicDataRepo.load() } }
    }

    val state = combine(
        profileRepo.observeApplication(),
        publicDataRepo.state,
    ) { app, result ->
        buildUiState(app, result)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DataUiState())

    private fun buildUiState(
        app: ApplicationProfile?,
        result: PublicDataResult?,
    ): DataUiState {
        val office = app?.submittedOffice
        val officeLabel = office?.let { context.getString(it.labelRes) } ?: ""
        val visaTypeLabel = app?.visaType?.let { context.getString(it.labelRes) } ?: ""
        val pathLabel = app?.applicationPath?.let { context.getString(it.labelRes) } ?: ""

        // Before the data resolves, show neutral defaults.
        val doc = result?.doc ?: return DataUiState(
            officeLabel = officeLabel,
            visaTypeLabel = visaTypeLabel,
            pathLabel = pathLabel,
            loading = true,
            regionalNote = PublicDataSource.forOffice(office).regionalNote,
        )

        val officeData: OfficeData? = office
            ?.takeIf { it != ImmigrationOffice.OTHER }
            ?.let { doc.officeData(it.name) }

        val stats = officeData?.monthly?.lastOrNull()?.let { latest ->
            val bureauLatest = officeData.bureauTotal?.monthly?.lastOrNull()
            OfficeStats(
                latestMonthLabel = monthLabel(latest.month),
                received = latest.received,
                processed = latest.processed,
                pending = latest.pending,
                // Calibrated to real latency so it matches the prediction page
                // (raw pending÷processed underestimates; see OfficeData.calibrationFactor).
                queueMonths = if (latest.processed > 0)
                    latest.pending.toDouble() / latest.processed * officeData.calibrationFactor
                else null,
                bureauLabel = officeData.bureauTotal?.label,
                bureauPending = bureauLatest?.pending,
            )
        }

        // Trend: prefer yearly approvals (年度许可数); else recent monthly processed.
        val permits = officeData?.permitsByYear.orEmpty()
        val (trendTitle, trendUnit, trend) = when {
            permits.isNotEmpty() -> Triple(
                context.getString(R.string.data_trend_yearly_title),
                context.getString(R.string.data_trend_yearly_unit),
                permits.takeLast(8).map { TrendBar("${it.year}", it.count) },
            )
            officeData != null && officeData.monthly.isNotEmpty() -> Triple(
                context.getString(R.string.data_trend_monthly_title),
                context.getString(R.string.data_trend_monthly_unit),
                officeData.monthly.takeLast(8).map { TrendBar(shortMonth(it.month), it.processed) },
            )
            else -> Triple("", "", emptyList())
        }

        return DataUiState(
            officeLabel = officeLabel,
            visaTypeLabel = visaTypeLabel,
            pathLabel = pathLabel,
            loading = false,
            standardRange = doc.standardProcessing.rangeLabel,
            standardMinMonths = doc.standardProcessing.minMonths,
            standardMaxMonths = doc.standardProcessing.maxMonths,
            sourceName = doc.source.name,
            dataAsOfLabel = monthLabel(doc.dataAsOf),
            originLabel = originLabel(result.origin),
            regionalNote = officeData?.regionalNote
                ?: PublicDataSource.forOffice(office).regionalNote,
            stats = stats,
            trendTitle = trendTitle,
            trendUnit = trendUnit,
            trend = trend,
        )
    }

    private fun originLabel(origin: PublicDataOrigin): String = when (origin) {
        PublicDataOrigin.NETWORK -> context.getString(R.string.data_origin_live)
        PublicDataOrigin.CACHE -> context.getString(R.string.data_origin_cached)
        PublicDataOrigin.BUNDLED -> context.getString(R.string.data_origin_bundled)
    }

    /** "2026-03" → locale-formatted year-month; passthrough if unparseable. */
    private fun monthLabel(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return context.getString(R.string.date_year_month_fmt, parts[0], month.toString())
    }

    /** "2026-03" → "26/3" for compact bar labels. */
    private fun shortMonth(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return "${parts[0].takeLast(2)}/$month"
    }
}
