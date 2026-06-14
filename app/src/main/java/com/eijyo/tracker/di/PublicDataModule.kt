package com.eijyo.tracker.di

import com.eijyo.tracker.data.local.DataStorePublicDataCache
import com.eijyo.tracker.data.local.PublicDataCache
import com.eijyo.tracker.data.remote.JsDelivrPublicDataRemote
import com.eijyo.tracker.data.remote.PublicDataRemote
import com.eijyo.tracker.data.staticdata.AssetsPublicDataBundled
import com.eijyo.tracker.data.staticdata.PublicDataBundled
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/** Binds the three fallback-chain implementations to their interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class PublicDataBindModule {

    @Binds
    abstract fun bindRemote(impl: JsDelivrPublicDataRemote): PublicDataRemote

    @Binds
    abstract fun bindCache(impl: DataStorePublicDataCache): PublicDataCache

    @Binds
    abstract fun bindBundled(impl: AssetsPublicDataBundled): PublicDataBundled
}

@Module
@InstallIn(SingletonComponent::class)
object PublicDataModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
