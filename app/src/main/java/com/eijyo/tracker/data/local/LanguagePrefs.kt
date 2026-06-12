package com.eijyo.tracker.data.local

import android.content.Context
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
}
