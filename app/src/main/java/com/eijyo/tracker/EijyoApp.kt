package com.eijyo.tracker

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.eijyo.tracker.data.local.LanguagePrefs
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

/**
 * Application entry point. [@HiltAndroidApp] triggers Hilt's code generation and
 * creates the application-level dependency container that every other component
 * (Activities, ViewModels, …) is built from.
 *
 * [attachBaseContext] localizes the **application** context to the saved app language so
 * the `@ApplicationContext` injected into ViewModels/domain (whose strings go through
 * `context.getString`) matches the Activity's localized resources — otherwise those layers
 * would fall back to the system locale (e.g. English) while Compose `stringResource` shows
 * Chinese. Runtime switches additionally refresh this via [applyLocale].
 */
@HiltAndroidApp
class EijyoApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguagePrefs.wrap(base))
    }

    /**
     * Re-applies the saved locale to the live application resources so already-created
     * `@ApplicationContext` references pick up a language change without a process restart.
     * Called from the settings flow right before the Activity recreates.
     */
    @Suppress("DEPRECATION")
    fun applyLocale() {
        val locale = LanguagePrefs.localeFor(LanguagePrefs.get(this))
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocales(LocaleList(locale))
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
