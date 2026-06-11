package com.eijyo.tracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Light-only macaron color scheme. The design ships a dark mode variable set, but the
 * brand identity (cream + pastel) is a light experience, so dark mode is deferred.
 * Material3 slots are mapped onto the macaron palette; richer semantic tones are
 * available via [EijyoTheme.colors].
 */
private val MacaronColorScheme = lightColorScheme(
    primary = MacaronPalette.Mint,
    onPrimary = MacaronPalette.White,
    primaryContainer = MacaronPalette.MintContainer,
    onPrimaryContainer = MacaronPalette.Ink,

    secondary = MacaronPalette.Coral,
    onSecondary = MacaronPalette.White,
    secondaryContainer = MacaronPalette.Peach,
    onSecondaryContainer = MacaronPalette.Ink,

    tertiary = MacaronPalette.LavenderAccent,
    onTertiary = MacaronPalette.White,
    tertiaryContainer = MacaronPalette.LavenderSoft,
    onTertiaryContainer = MacaronPalette.Ink,

    background = MacaronPalette.Cream,
    onBackground = MacaronPalette.Ink,
    surface = MacaronPalette.CreamSoft,
    onSurface = MacaronPalette.Ink,
    surfaceVariant = MacaronPalette.CreamCard,
    onSurfaceVariant = MacaronPalette.InkMuted,

    outline = MacaronPalette.Border,
    outlineVariant = MacaronPalette.Border,

    error = MacaronPalette.Coral,
    onError = MacaronPalette.White,
)

@Composable
fun EijyoTrackerTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMacaronColors provides LightMacaronColors) {
        MaterialTheme(
            colorScheme = MacaronColorScheme,
            typography = EijyoTypography,
            content = content,
        )
    }
}

/** Convenience accessor mirroring `MaterialTheme`, e.g. `EijyoTheme.colors.mint`. */
object EijyoTheme {
    val colors: MacaronColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMacaronColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

/** Shared corner-radius and spacing tokens echoing the design's `--radius-*` variables. */
object EijyoTokens {
    const val RadiusCard = 24
    const val RadiusButton = 28
    const val RadiusPill = 999
    const val RadiusChip = 16

    val OverlayScrim = Color(0x66000000)
}
