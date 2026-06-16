package com.eijyo.tracker.prediction

import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.ConfidenceLevel
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.MonthlyPoint
import com.eijyo.tracker.data.model.OfficeData
import com.eijyo.tracker.data.model.PublicDataDoc
import com.eijyo.tracker.data.model.StandardProcessing
import com.eijyo.tracker.data.model.DataSource
import com.eijyo.tracker.data.model.TriState
import android.content.Context
import com.eijyo.tracker.domain.prediction.PredictionEngine
import com.eijyo.tracker.domain.prediction.WaitState
import com.eijyo.tracker.domain.prediction.computeWait
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD tests for the FIFO PredictionEngine (§10.1 of PREDICTION_AND_DATA.md).
 *
 * Cases A-D test the pure [computeWait] function.
 * Cases E-G test [PredictionEngine.predict] (PublicDataDoc overload).
 *
 * ALL TESTS ARE RED until Step 5 implements the FIFO algorithm.
 * Expected values are hand-calculated from §6 formulas; do NOT change them —
 * change the implementation to match.
 */
class PredictionEngineTest {

    // ── Base fixture ─────────────────────────────────────────────────────────
    //
    // office="TEST", dataAsOf=2026-03, 6 months of data:
    //   month     received  processed  pending
    //   2025-10   1000      800        10000
    //   2025-11   1000      1200       10000
    //   2025-12   1000      1000       12000
    //   2026-01   1000      1200       12000
    //   2026-02   1000      800        12000
    //   2026-03   1000      1000       12000
    //
    // Last 6-month processed window: [800,1200,1000,1200,800,1000]
    //   → μ_latest = 1000 (2026-03)
    //   → μ_fast   = 1200 (max)
    //   → μ_slow   = 800  (min)

    private val baseSeries = listOf(
        MonthlyPoint(month = "2025-10", received = 1000, processed = 800,  pending = 10000),
        MonthlyPoint(month = "2025-11", received = 1000, processed = 1200, pending = 10000),
        MonthlyPoint(month = "2025-12", received = 1000, processed = 1000, pending = 12000),
        MonthlyPoint(month = "2026-01", received = 1000, processed = 1200, pending = 12000),
        MonthlyPoint(month = "2026-02", received = 1000, processed = 800,  pending = 12000),
        MonthlyPoint(month = "2026-03", received = 1000, processed = 1000, pending = 12000),
    )

    // ── Case A — Normal: region divides cleanly ───────────────────────────────
    //
    // submitMonth=2025-12, today=2026-06-15
    //   Q0      = pending[2025-12]            = 12000
    //   ΣP      = processed[2026-01..03]      = 1200+800+1000 = 3000
    //   R_latest= 12000 - 3000               = 9000
    //   gap     = months(2026-03 → 2026-06)  = 3
    //   μ_latest= processed[2026-03]         = 1000
    //   R_now   = 9000 - 1000×3             = 6000
    //   →  normalMonths       = 6000/1000 = 6.0
    //      optimisticMonths   = 6000/1200 = 5.0
    //      conservativeMonths = 6000/800  = 7.5

    @Test
    fun `Case A - rNow equals 6000`() {
        val result = computeWait(baseSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(6000, result.rNow)
    }

    @Test
    fun `Case A - normalMonths equals 6_0`() {
        val result = computeWait(baseSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(6.0, result.normalMonths, 0.01)
    }

    @Test
    fun `Case A - optimisticMonths equals 5_0`() {
        val result = computeWait(baseSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(5.0, result.optimisticMonths, 0.01)
    }

    @Test
    fun `Case A - conservativeMonths equals 7_5`() {
        val result = computeWait(baseSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(7.5, result.conservativeMonths, 0.01)
    }

    @Test
    fun `Case A - state is WAITING`() {
        val result = computeWait(baseSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(WaitState.WAITING, result.state)
    }

    // ── Case B — Submit month earlier than series start ───────────────────────
    //
    // submitMonth=2025-06 (< 2025-10 = earliest in series), today=2026-06-15
    //   Q0      = pending[2025-10]  (fallback: earliest)  = 10000
    //   ΣP      = processed[2025-11..03]  (from earliest+1 month)
    //           = 1200+1000+1200+800+1000                = 5200
    //   R_latest= 10000 - 5200                           = 4800
    //   R_now   = 4800 - 1000×3                          = 1800
    //   → normalMonths = 1800/1000 = 1.8

    @Test
    fun `Case B - early submit uses earliest pending as Q0`() {
        val result = computeWait(baseSeries, submitMonth = "2025-06", today = LocalDate.of(2026, 6, 15))
        assertEquals(1800, result.rNow)
    }

    @Test
    fun `Case B - normalMonths equals 1_8`() {
        val result = computeWait(baseSeries, submitMonth = "2025-06", today = LocalDate.of(2026, 6, 15))
        assertEquals(1.8, result.normalMonths, 0.01)
    }

    @Test
    fun `Case B - state is WAITING`() {
        val result = computeWait(baseSeries, submitMonth = "2025-06", today = LocalDate.of(2026, 6, 15))
        assertEquals(WaitState.WAITING, result.state)
    }

    // ── Case C — R_now ≤ 0 (already due) ─────────────────────────────────────
    //
    // Same base series but pending[2025-12] = 2000, submitMonth=2025-12, today=2026-06-15
    //   Q0      = pending[2025-12]        = 2000
    //   ΣP      = processed[2026-01..03] = 3000
    //   R_latest= 2000 - 3000            = -1000
    //   R_now   = -1000 - 1000×3         = -4000
    //   → state = ALREADY_DUE

    private val caseCSeries = baseSeries.map {
        if (it.month == "2025-12") it.copy(pending = 2000) else it
    }

    @Test
    fun `Case C - rNow is negative`() {
        val result = computeWait(caseCSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertTrue("rNow should be negative, was ${result.rNow}", result.rNow <= 0)
    }

    @Test
    fun `Case C - state is ALREADY_DUE`() {
        val result = computeWait(caseCSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(WaitState.ALREADY_DUE, result.state)
    }

    @Test
    fun `Case C - months fields are not NaN or infinite`() {
        val result = computeWait(caseCSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertFalse(result.normalMonths.isNaN())
        assertFalse(result.normalMonths.isInfinite())
    }

    // ── Case D — μ_latest == 0 (divide-by-zero guard) ────────────────────────
    //
    // Base series but processed[2026-03] = 0
    //   μ_latest = 0 → fallback to avg of last 6 months
    //   avg = (800+1200+1000+1200+800+0) / 6 = 5000/6 ≈ 833
    //   ΣP[2026-01..03] = 1200+800+0 = 2000
    //   R_latest = 12000 - 2000 = 10000
    //   R_now    = 10000 - 833×3 ≈ 7501
    //   normalMonths ≈ 7501/833 ≈ 9.0

    private val caseDSeries = baseSeries.map {
        if (it.month == "2026-03") it.copy(processed = 0) else it
    }

    @Test
    fun `Case D - no crash when mu_latest is zero`() {
        val result = computeWait(caseDSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertFalse(result.normalMonths.isNaN())
        assertFalse(result.normalMonths.isInfinite())
    }

    @Test
    fun `Case D - normalMonths uses fallback average, not zero`() {
        val result = computeWait(caseDSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertTrue("normalMonths should be > 0, was ${result.normalMonths}", result.normalMonths > 0.0)
    }

    @Test
    fun `Case D - normalMonths approximately 9_0 (fallback avg=833)`() {
        val result = computeWait(caseDSeries, submitMonth = "2025-12", today = LocalDate.of(2026, 6, 15))
        assertEquals(9.0, result.normalMonths, 1.0)
    }

    // ── Cases E–G: PredictionEngine.predict(profile, PublicDataDoc, today) ───

    // Context is only used to format office names into the reasons list; a relaxed mock
    // returning a stub string is enough for the prediction-logic assertions below.
    private val context = mockk<Context>(relaxed = true).also {
        every { it.getString(any()) } returns "office"
        every { it.getString(any(), *anyVararg()) } returns "office"
    }
    private val engine = PredictionEngine(context)

    private fun minimalDoc(vararg offices: OfficeData) = PublicDataDoc(
        schemaVersion = 1,
        dataAsOf = "2026-03",
        updatedAt = "2026-06-14",
        source = DataSource(
            name = "e-Stat",
            mainTableUrl = "https://www.e-stat.go.jp/dbview?sid=0003449073",
            permitTableUrl = "https://www.e-stat.go.jp/dbview?sid=0003289203",
        ),
        standardProcessing = StandardProcessing(
            rangeLabel = "4 - 6 个月",
            minMonths = 4,
            maxMonths = 6,
        ),
        offices = offices.toList(),
    )

    private val tokyoOffice = OfficeData(
        code = "TOKYO",
        displayName = "东京入管",
        regionalNote = "",
        monthly = baseSeries,
    )

    private fun reviewingProfile(
        office: ImmigrationOffice? = ImmigrationOffice.TOKYO,
        submitDate: String = "2025-12-15",
        precision: DatePrecision = DatePrecision.DAY,
        supplement: TriState = TriState.NO,
    ) = ApplicationProfile(
        status = ApplicationStatus.REVIEWING,
        submittedOffice = office,
        submittedDate = submitDate,
        submittedDatePrecision = precision,
        hasSupplementRequest = supplement,
    )

    // ── Case E — OTHER / empty series → old 4-6 month fallback ───────────────
    //
    // When the user's office has no monthly data (empty series) or is OTHER,
    // predict() must NOT return null — it falls back to the old 4-6 month logic.

    @Test
    fun `Case E - office OTHER returns non-null fallback prediction`() {
        val doc = minimalDoc()  // no offices → TOKYO lookup returns null → fallback
        val profile = reviewingProfile(office = ImmigrationOffice.OTHER)
        val result = engine.predict(profile, doc, today = LocalDate.of(2026, 6, 15))
        assertNotNull("OTHER office must get a fallback prediction, not null", result)
    }

    @Test
    fun `Case E - office with empty series returns non-null fallback prediction`() {
        val emptyOffice = tokyoOffice.copy(monthly = emptyList())
        val doc = minimalDoc(emptyOffice)
        val profile = reviewingProfile(office = ImmigrationOffice.TOKYO)
        val result = engine.predict(profile, doc, today = LocalDate.of(2026, 6, 15))
        assertNotNull("Empty series must get a fallback prediction, not null", result)
    }

    // ── Case F — Confidence level tiers ──────────────────────────────────────
    //
    // New confidence factors (in addition to existing precision/supplement checks):
    //   dataFreshness: today - dataAsOf
    //     < 6 months  → no penalty
    //     6-12 months → -1
    //     > 12 months → -2 (or straight LOW)
    //   rNow near 0   → -1
    //   no regional data → straight LOW
    //
    // Baseline (all good):  DAY + fresh + has data + rNow > 0 → HIGH
    // Month precision only: MONTH + fresh + has data          → MEDIUM
    // Very stale data:      DAY + dataAsOf very old (15 months ago) → downgraded

    @Test
    fun `Case F - DAY precision fresh data has region data = HIGH`() {
        val doc = minimalDoc(tokyoOffice)
        // today=2026-04-15: dataAsOf=2026-03, gap only 1 month → fresh
        val profile = reviewingProfile(precision = DatePrecision.DAY)
        val result = engine.predict(profile, doc, today = LocalDate.of(2026, 4, 15))
        assertNotNull(result)
        assertEquals(ConfidenceLevel.HIGH, result!!.confidenceLevel)
    }

    @Test
    fun `Case F - MONTH precision fresh data = MEDIUM`() {
        val doc = minimalDoc(tokyoOffice)
        val profile = reviewingProfile(
            submitDate = "2025-12",
            precision = DatePrecision.MONTH,
        )
        val result = engine.predict(profile, doc, today = LocalDate.of(2026, 4, 15))
        assertNotNull(result)
        assertEquals(ConfidenceLevel.MEDIUM, result!!.confidenceLevel)
    }

    @Test
    fun `Case F - no regional data = LOW`() {
        val doc = minimalDoc()  // no matching office entry
        val profile = reviewingProfile(office = ImmigrationOffice.TOKYO)
        val result = engine.predict(profile, doc, today = LocalDate.of(2026, 4, 15))
        assertNotNull(result)
        assertEquals(ConfidenceLevel.LOW, result!!.confidenceLevel)
    }

    @Test
    fun `Case F - stale data 15 months ago = downgraded from HIGH`() {
        val doc = minimalDoc(tokyoOffice)
        // today = 2027-06-15, dataAsOf = 2026-03 → 15 months stale
        val profile = reviewingProfile(precision = DatePrecision.DAY)
        val result = engine.predict(profile, doc, today = LocalDate.of(2027, 6, 15))
        assertNotNull(result)
        assertTrue(
            "15-month-stale data must not be HIGH, was ${result!!.confidenceLevel}",
            result.confidenceLevel != ConfidenceLevel.HIGH,
        )
    }

    // ── Case G — Non-reviewing status → null ─────────────────────────────────

    @Test
    fun `Case G - PREPARING status returns null`() {
        val doc = minimalDoc(tokyoOffice)
        val profile = ApplicationProfile(
            status = ApplicationStatus.PREPARING,
            submittedOffice = ImmigrationOffice.TOKYO,
            submittedDate = "2025-12-15",
            submittedDatePrecision = DatePrecision.DAY,
        )
        assertNull(engine.predict(profile, doc, today = LocalDate.of(2026, 6, 15)))
    }

    @Test
    fun `Case G - COMPLETED status returns null`() {
        val doc = minimalDoc(tokyoOffice)
        val profile = ApplicationProfile(
            status = ApplicationStatus.COMPLETED,
            submittedOffice = ImmigrationOffice.TOKYO,
            submittedDate = "2025-12-15",
            submittedDatePrecision = DatePrecision.DAY,
        )
        assertNull(engine.predict(profile, doc, today = LocalDate.of(2026, 6, 15)))
    }
}
