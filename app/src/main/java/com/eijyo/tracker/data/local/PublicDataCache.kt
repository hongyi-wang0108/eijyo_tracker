package com.eijyo.tracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Tier 2 of the public-data fallback chain: the last JSON successfully fetched. */
interface PublicDataCache {
    /** Returns the cached raw JSON, or null if nothing has been cached yet. */
    suspend fun read(): String?

    /** Persists the latest raw JSON so a later offline launch can reuse it. */
    suspend fun write(json: String)
}

// Separate DataStore file from AppPreferences' "eijyo_prefs": two delegates on the
// same name in one process crash. Stored in DataStore (not Room) so it never forces a
// Room version bump — which, with fallbackToDestructiveMigration, would wipe user data.
private val Context.publicDataStore by preferencesDataStore(name = "public_data_cache")

@Singleton
class DataStorePublicDataCache @Inject constructor(
    @ApplicationContext private val context: Context,
) : PublicDataCache {

    private object Keys {
        val RAW_JSON = stringPreferencesKey("public_data_raw_json")
    }

    override suspend fun read(): String? =
        context.publicDataStore.data.map { it[Keys.RAW_JSON] }.first()

    override suspend fun write(json: String) {
        context.publicDataStore.edit { it[Keys.RAW_JSON] = json }
    }
}
