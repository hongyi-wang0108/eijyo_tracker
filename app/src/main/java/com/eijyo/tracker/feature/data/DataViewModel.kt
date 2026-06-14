package com.eijyo.tracker.feature.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.OfficeData
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.PublicDataOrigin
import com.eijyo.tracker.data.repository.PublicDataRepository
import com.eijyo.tracker.data.repository.PublicDataResult
import com.eijyo.tracker.data.staticdata.PublicDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val officeLabel = office?.label ?: ""
        val visaTypeLabel = app?.visaType?.label ?: ""
        val pathLabel = app?.applicationPath?.label ?: ""

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
                queueMonths = if (latest.processed > 0)
                    latest.pending.toDouble() / latest.processed else null,
                bureauLabel = officeData.bureauTotal?.label,
                bureauPending = bureauLatest?.pending,
            )
        }

        // Trend: prefer yearly approvals (年度许可数); else recent monthly processed.
        val permits = officeData?.permitsByYear.orEmpty()
        val (trendTitle, trendUnit, trend) = when {
            permits.isNotEmpty() -> Triple(
                "年度永住许可人数",
                "人/年",
                permits.takeLast(8).map { TrendBar("${it.year}", it.count) },
            )
            officeData != null && officeData.monthly.isNotEmpty() -> Triple(
                "近月处理人数",
                "人/月",
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
        PublicDataOrigin.NETWORK -> "最新数据"
        PublicDataOrigin.CACHE -> "缓存数据"
        PublicDataOrigin.BUNDLED -> "内置数据"
    }

    /** "2026-03" → "2026年3月"; passthrough if unparseable. */
    private fun monthLabel(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return "${parts[0]}年${month}月"
    }

    /** "2026-03" → "26/3" for compact bar labels. */
    private fun shortMonth(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return "${parts[0].takeLast(2)}/$month"
    }
}
