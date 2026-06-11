package com.eijyo.tracker.feature.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eijyo.tracker.core.ui.component.DogMascot
import com.eijyo.tracker.core.ui.theme.EijyoTheme

/** Honest placeholder for tabs not yet built, so navigation works end-to-end. */
@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    val colors = EijyoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DogMascot(size = 120.dp, wagging = false)
        Text(
            text = title,
            style = EijyoTheme.typography.headlineMedium,
            color = colors.ink,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = subtitle,
            style = EijyoTheme.typography.bodyMedium,
            color = colors.inkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
