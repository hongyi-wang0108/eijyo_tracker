package com.eijyo.tracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eijyo.tracker.feature.home.MainScaffold
import com.eijyo.tracker.feature.onboarding.OnboardingCompleteScreen
import com.eijyo.tracker.feature.onboarding.OnboardingScreen
import com.eijyo.tracker.feature.welcome.WelcomeScreen

/**
 * Top-level navigation graph. Flow: Welcome → Onboarding → Onboarding Complete →
 * Main (bottom-tab shell). Returning users start directly at Main.
 */
@Composable
fun EijyoNavHost(rootViewModel: RootViewModel = hiltViewModel()) {
    val startRoute by rootViewModel.startRoute.collectAsStateWithLifecycle()

    // Blank splash while the onboarding flag resolves.
    val resolved = startRoute ?: run {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    // Resolve the start destination exactly once. The onboarding-completed flag flips
    // to true on the final question, which would otherwise retarget this live NavHost to
    // MAIN and skip the Onboarding Complete screen entirely.
    val start = rememberSaveable { resolved }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = start) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStart = { navController.navigate(Routes.ONBOARDING) },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.ONBOARDING_COMPLETE) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onExit = { navController.popBackStack() },
            )
        }
        composable(Routes.ONBOARDING_COMPLETE) {
            OnboardingCompleteScreen(
                onEnterHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.MAIN) {
            MainScaffold()
        }
    }
}
