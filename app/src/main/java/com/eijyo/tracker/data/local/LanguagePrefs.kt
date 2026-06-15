package com.eijyo.tracker.data.local

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LanguagePrefs {
    private const val PREFS = "eijyo_lang"
    private const val KEY = "code"

    fun get(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "zh") ?: "zh"

    fun set(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, code).apply()
    }

    fun localeFor(code: String): Locale = when (code) {
        "ja" -> Locale.JAPANESE
        "en" -> Locale.ENGLISH
        else -> Locale.SIMPLIFIED_CHINESE
    }

    /**
     * Returns a context whose resources resolve to the saved app language, overriding the
     * system locale. Used by both [com.eijyo.tracker.MainActivity] (Activity resources,
     * for `stringResource`) and [com.eijyo.tracker.EijyoApp] (Application/`@ApplicationContext`
     * resources, for ViewModel/domain `context.getString`) so every layer agrees.
     */
    fun wrap(base: Context): Context {
        val locale = localeFor(get(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }
}
