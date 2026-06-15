package com.eijyo.tracker.navigation

import androidx.annotation.StringRes
import com.eijyo.tracker.R

/** Centralized navigation route keys. */
object Routes {
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"
    const val ONBOARDING_COMPLETE = "onboarding_complete"
    const val MAIN = "main"
    const val PREDICTION_DETAIL = "prediction_detail"
    const val RISK_DETAIL = "risk_detail"
}

/** Bottom-tab destinations inside the main app shell. */
enum class MainTab(val route: String, @StringRes val labelRes: Int) {
    HOME("home", R.string.tab_home),
    APPLICATION("application", R.string.tab_application),
    DOCUMENTS("documents", R.string.tab_documents),
    DATA("data", R.string.tab_data),
    SETTINGS("settings", R.string.tab_settings),
}
