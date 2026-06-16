package com.eijyo.tracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/** Tier 1 of the public-data fallback chain: fetch the raw JSON from the CDN. */
interface PublicDataRemote {
    /** Returns the raw JSON text, or throws on any network/HTTP failure. */
    suspend fun fetchJson(): String
}

/**
 * Fetches public-data.json from jsDelivr (CDN over the GitHub data repo).
 * No Retrofit/OkHttp dependency — a single static GET via [HttpURLConnection] is enough.
 * See docs/PREDICTION_AND_DATA.md §1.1 for why jsDelivr over raw.githubusercontent.
 */
class JsDelivrPublicDataRemote @Inject constructor() : PublicDataRemote {

    override suspend fun fetchJson(): String = withContext(Dispatchers.IO) {
        val connection = (URL(URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IOException("public-data fetch failed: HTTP $code")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val URL =
            "https://cdn.jsdelivr.net/gh/hongyi-wang0108/immigration-data-jp@main/public-data.json"
        private const val TIMEOUT_MS = 8_000
    }
}
