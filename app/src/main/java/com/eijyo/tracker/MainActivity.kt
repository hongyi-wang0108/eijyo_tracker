package com.eijyo.tracker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eijyo.tracker.core.ui.theme.EijyoTrackerTheme
import com.eijyo.tracker.data.local.LanguagePrefs
import com.eijyo.tracker.navigation.EijyoNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host. All screens live inside a Compose [EijyoNavHost];
 * navigation between them is handled by Navigation Compose, not Activities/Fragments.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val code = LanguagePrefs.get(newBase)
        val locale = LanguagePrefs.localeFor(code)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EijyoTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EijyoNavHost()
                }
            }
        }
    }
}
