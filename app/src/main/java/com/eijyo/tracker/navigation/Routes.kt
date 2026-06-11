package com.eijyo.tracker.navigation

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
enum class MainTab(val route: String, val label: String) {
    HOME("home", "首页"),
    APPLICATION("application", "申请"),
    DOCUMENTS("documents", "材料"),
    DATA("data", "数据"),
    SETTINGS("settings", "我的"),
}
