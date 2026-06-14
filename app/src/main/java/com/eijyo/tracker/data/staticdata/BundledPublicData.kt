package com.eijyo.tracker.data.staticdata

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Tier 3 (final fallback) of the public-data chain: the public-data.json shipped inside
 * the APK at assets/public-data.json. Always available, so the app never shows a blank
 * data screen — even on a fresh install with no network. Updated whenever a release is cut.
 */
interface PublicDataBundled {
    /** Reads the bundled JSON from APK assets. */
    fun readJson(): String
}

class AssetsPublicDataBundled @Inject constructor(
    @ApplicationContext private val context: Context,
) : PublicDataBundled {

    override fun readJson(): String =
        context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }

    companion object {
        const val ASSET_NAME = "public-data.json"
    }
}
