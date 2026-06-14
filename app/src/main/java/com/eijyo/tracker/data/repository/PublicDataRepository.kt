package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.PublicDataCache
import com.eijyo.tracker.data.model.PublicDataDoc
import com.eijyo.tracker.data.remote.PublicDataRemote
import com.eijyo.tracker.data.staticdata.PublicDataBundled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Where the currently-held public data came from — useful for UI freshness hints. */
enum class PublicDataOrigin { NETWORK, CACHE, BUNDLED }

data class PublicDataResult(
    val doc: PublicDataDoc,
    val origin: PublicDataOrigin,
)

/**
 * Single source of truth for the official public data, with a three-tier fallback
 * (docs/PREDICTION_AND_DATA.md §1):
 *
 *   1. NETWORK  — fetch from jsDelivr; on success, cache the raw JSON and return it.
 *   2. CACHE    — last successfully fetched JSON (offline / network failure).
 *   3. BUNDLED  — APK assets copy; always parseable, so [load] never throws.
 *
 * A malformed payload at any tier is treated as a miss and falls through to the next.
 */
@Singleton
class PublicDataRepository @Inject constructor(
    private val remote: PublicDataRemote,
    private val cache: PublicDataCache,
    private val bundled: PublicDataBundled,
    private val json: Json,
) {
    private val _state = MutableStateFlow<PublicDataResult?>(null)

    /** Latest loaded result, or null before the first [load]. */
    val state: StateFlow<PublicDataResult?> = _state.asStateFlow()

    /**
     * Resolves the best available public data, walking the fallback chain.
     * Never throws: the bundled asset is the guaranteed floor.
     */
    suspend fun load(): PublicDataResult {
        // Prime with the bundled (real) data immediately so the UI shows real numbers at
        // once instead of staying empty while the network attempt is in flight. The tiers
        // below upgrade to network/cache when available.
        if (_state.value == null) {
            runCatching { parse(bundled.readJson()) }.getOrNull()
                ?.let { _state.value = PublicDataResult(it, PublicDataOrigin.BUNDLED) }
        }

        // Tier 1: network. Parse before trusting it; cache only valid JSON.
        runCatching { remote.fetchJson() }
            .mapCatching { raw -> parse(raw) to raw }
            .getOrNull()
            ?.let { (doc, raw) ->
                runCatching { cache.write(raw) } // cache best-effort; failure mustn't break load
                return finish(doc, PublicDataOrigin.NETWORK)
            }

        // Tier 2: cache.
        runCatching { cache.read() }.getOrNull()
            ?.let { raw -> runCatching { parse(raw) }.getOrNull() }
            ?.let { doc -> return finish(doc, PublicDataOrigin.CACHE) }

        // Tier 3: bundled asset (guaranteed).
        return finish(parse(bundled.readJson()), PublicDataOrigin.BUNDLED)
    }

    private fun finish(doc: PublicDataDoc, origin: PublicDataOrigin): PublicDataResult =
        PublicDataResult(doc, origin).also { _state.value = it }

    private fun parse(raw: String): PublicDataDoc =
        json.decodeFromString(PublicDataDoc.serializer(), raw)
}
