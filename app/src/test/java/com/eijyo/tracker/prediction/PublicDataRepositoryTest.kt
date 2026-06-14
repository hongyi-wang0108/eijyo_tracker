package com.eijyo.tracker.prediction

import com.eijyo.tracker.data.local.PublicDataCache
import com.eijyo.tracker.data.remote.PublicDataRemote
import com.eijyo.tracker.data.repository.PublicDataOrigin
import com.eijyo.tracker.data.repository.PublicDataRepository
import com.eijyo.tracker.data.staticdata.PublicDataBundled
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.io.IOException

/**
 * §10.3 — three-tier fallback chain smoke tests, using fakes for all three sources.
 * Pure JVM (no Android), so the orchestration in PublicDataRepository is verified directly.
 */
class PublicDataRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // Real bundled asset reused as the guaranteed floor in tests.
    private val bundledJson: String = File("src/main/assets/public-data.json").readText()

    // ── Fakes ──────────────────────────────────────────────────────────────────

    private class FakeRemote(
        private val payload: String? = null,
        private val error: Throwable? = null,
    ) : PublicDataRemote {
        var calls = 0; private set
        override suspend fun fetchJson(): String {
            calls++
            error?.let { throw it }
            return payload ?: throw IOException("no payload")
        }
    }

    private class FakeCache(initial: String? = null) : PublicDataCache {
        var stored: String? = initial; private set
        var writes = 0; private set
        override suspend fun read(): String? = stored
        override suspend fun write(json: String) { stored = json; writes++ }
    }

    private inner class FakeBundled : PublicDataBundled {
        override fun readJson(): String = bundledJson
    }

    private fun repo(
        remote: PublicDataRemote,
        cache: PublicDataCache,
        bundled: PublicDataBundled = FakeBundled(),
    ) = PublicDataRepository(remote, cache, bundled, json)

    // A minimal but valid alternate doc so we can tell network/cache apart from bundled.
    private val altJson = """
        {
          "schemaVersion": 1,
          "dataAsOf": "2099-12",
          "updatedAt": "2099-12-31",
          "source": { "name": "x", "mainTableUrl": "u", "permitTableUrl": "u" },
          "standardProcessing": { "rangeLabel": "x", "minMonths": 4, "maxMonths": 6 },
          "offices": []
        }
    """.trimIndent()

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    fun `network success returns network data and caches raw`() = runBlocking {
        val cache = FakeCache()
        val result = repo(FakeRemote(payload = altJson), cache).load()

        assertEquals(PublicDataOrigin.NETWORK, result.origin)
        assertEquals("2099-12", result.doc.dataAsOf)
        assertEquals("network payload should be cached", altJson, cache.stored)
        assertEquals(1, cache.writes)
    }

    @Test
    fun `network failure falls back to cache`() = runBlocking {
        val cache = FakeCache(initial = altJson)
        val result = repo(FakeRemote(error = IOException("offline")), cache).load()

        assertEquals(PublicDataOrigin.CACHE, result.origin)
        assertEquals("2099-12", result.doc.dataAsOf)
    }

    @Test
    fun `network failure and empty cache falls back to bundled`() = runBlocking {
        val result = repo(FakeRemote(error = IOException("offline")), FakeCache()).load()

        assertEquals(PublicDataOrigin.BUNDLED, result.origin)
        assertEquals("2026-03", result.doc.dataAsOf) // real bundled value
    }

    @Test
    fun `malformed network payload falls through to cache`() = runBlocking {
        val cache = FakeCache(initial = altJson)
        val result = repo(FakeRemote(payload = "{ not valid json"), cache).load()

        assertEquals(PublicDataOrigin.CACHE, result.origin)
        assertEquals("malformed payload must not overwrite cache", altJson, cache.stored)
    }

    @Test
    fun `malformed network and malformed cache falls through to bundled`() = runBlocking {
        val cache = FakeCache(initial = "{ also broken")
        val result = repo(FakeRemote(payload = "{ broken"), cache).load()

        assertEquals(PublicDataOrigin.BUNDLED, result.origin)
        assertEquals("2026-03", result.doc.dataAsOf)
    }

    @Test
    fun `state flow is updated after load`() = runBlocking {
        val repository = repo(FakeRemote(payload = altJson), FakeCache())
        assertNull("state starts null before any load", repository.state.value)
        repository.load()
        assertEquals(PublicDataOrigin.NETWORK, repository.state.value?.origin)
    }
}
