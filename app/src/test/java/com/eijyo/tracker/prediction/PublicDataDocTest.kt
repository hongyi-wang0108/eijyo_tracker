package com.eijyo.tracker.prediction

import com.eijyo.tracker.data.model.PublicDataDoc
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Verifies that the bundled public-data.json parses into PublicDataDoc without error
 * and that the 2026-03 gold-standard values (§10.2 of PREDICTION_AND_DATA.md) are correct.
 *
 * Uses the real file from app/src/main/assets (path resolved relative to project root).
 */
class PublicDataDocTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val doc: PublicDataDoc by lazy {
        val assetFile = File("src/main/assets/public-data.json")
        assertTrue("assets/public-data.json must exist", assetFile.exists())
        json.decodeFromString(PublicDataDoc.serializer(), assetFile.readText())
    }

    @Test
    fun `parses top-level fields`() {
        assertEquals(1, doc.schemaVersion)
        assertEquals("2026-03", doc.dataAsOf)
        assertTrue(doc.offices.isNotEmpty())
    }

    @Test
    fun `all expected office codes present`() {
        val codes = doc.offices.map { it.code }.toSet()
        for (expected in listOf("TOKYO", "YOKOHAMA", "OSAKA", "KOBE", "NAGOYA", "FUKUOKA")) {
            assertTrue("Missing office $expected", expected in codes)
        }
    }

    // ── §10.2 Gold standard: Tokyo 2026-03 pure values ────────────────────────

    @Test
    fun `tokyo pure 2026-03 received == 4423`() {
        val pt = doc.officeData("TOKYO")!!.monthly.last { it.month == "2026-03" }
        assertEquals(4423, pt.received)
    }

    @Test
    fun `tokyo pure 2026-03 processed == 3155`() {
        val pt = doc.officeData("TOKYO")!!.monthly.last { it.month == "2026-03" }
        assertEquals(3155, pt.processed)
    }

    @Test
    fun `tokyo pure 2026-03 pending == 46300`() {
        val pt = doc.officeData("TOKYO")!!.monthly.last { it.month == "2026-03" }
        assertEquals(46300, pt.pending)
    }

    @Test
    fun `tokyo bureauTotal 2026-03 pending == 51707`() {
        val bt = doc.officeData("TOKYO")!!.bureauTotal!!.monthly.last { it.month == "2026-03" }
        assertEquals(51707, bt.pending)
    }

    @Test
    fun `tokyo has 65 monthly data points`() {
        assertEquals(65, doc.officeData("TOKYO")!!.monthly.size)
    }

    // ── Structure invariants ───────────────────────────────────────────────────

    @Test
    fun `yokohama has no bureauTotal`() {
        assertNull(doc.officeData("YOKOHAMA")!!.bureauTotal)
    }

    @Test
    fun `pending == recv_total minus processed invariant does not go negative`() {
        for (office in doc.offices) {
            for (pt in office.monthly) {
                assertTrue(
                    "${office.code} ${pt.month}: pending=${pt.pending} must be >= 0",
                    pt.pending >= 0,
                )
            }
            office.bureauTotal?.monthly?.forEach { pt ->
                assertTrue(
                    "${office.code} bureauTotal ${pt.month}: pending=${pt.pending} must be >= 0",
                    pt.pending >= 0,
                )
            }
        }
    }

    @Test
    fun `tokyo permitsByYear contains 2024`() {
        val permits = doc.officeData("TOKYO")!!.permitsByYear!!
        val y2024 = permits.firstOrNull { it.year == 2024 }
        assertNotNull("2024 permits entry missing", y2024)
        assertEquals(17903, y2024!!.count)
    }
}
