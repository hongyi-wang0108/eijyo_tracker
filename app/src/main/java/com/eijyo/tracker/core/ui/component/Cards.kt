package com.eijyo.tracker.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.EijyoTokens

/**
 * Standard cream surface card with the design's soft rounded corners. Optional
 * [accent] left-tints the surface for the pastel-tone cards (mint/peach/sky/...).
 */
@Composable
fun MacaronCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Int = 20,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(EijyoTokens.RadiusCard.dp)
    val colors = CardDefaults.cardColors(
        containerColor = EijyoTheme.colors.card,
        contentColor = EijyoTheme.colors.ink,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            Column(modifier = Modifier.padding(contentPadding.dp), content = content)
        }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            Column(modifier = Modifier.padding(contentPadding.dp), content = content)
        }
    }
}

/** Small label above a card's main value, in the muted ink color. */
@Composable
fun CardLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = EijyoTheme.typography.labelMedium,
        color = EijyoTheme.colors.inkMuted,
        modifier = modifier,
    )
}
