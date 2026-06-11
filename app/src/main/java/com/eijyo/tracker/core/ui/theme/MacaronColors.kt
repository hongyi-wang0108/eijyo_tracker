package com.eijyo.tracker.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended semantic color set for the macaron theme, beyond what Material3's
 * ColorScheme exposes. Accessed in composables via `MaterialTheme`-style:
 * `EijyoTheme.colors.peach`, backed by [LocalMacaronColors].
 *
 * Each "tone" pairs a soft container color with a stronger accent/foreground,
 * matching how the design uses pastel cards with a saturated icon/label.
 */
@Immutable
data class MacaronColors(
    val screen: Color,
    val card: Color,
    val cardAlt: Color,

    val mint: Color,
    val mintContainer: Color,
    val mintSoft: Color,
    val mintWash: Color,

    val peach: Color,
    val coral: Color,
    val pink: Color,
    val pinkAccent: Color,
    val blush: Color,

    val skySoft: Color,
    val skyAccent: Color,

    val lavenderSoft: Color,
    val lavenderAccent: Color,

    val lemonSoft: Color,
    val lemonAccent: Color,

    val ink: Color,
    val inkMuted: Color,
    val border: Color,

    val dogBody: Color,
    val dogEar: Color,
    val dogMuzzle: Color,
    val dogFeature: Color,
    val dogPaw: Color,

    val shadow: Color,
)

val LightMacaronColors = MacaronColors(
    screen = MacaronPalette.Cream,
    card = MacaronPalette.CreamSoft,
    cardAlt = MacaronPalette.CreamCard,
    mint = MacaronPalette.Mint,
    mintContainer = MacaronPalette.MintContainer,
    mintSoft = MacaronPalette.MintSoft,
    mintWash = MacaronPalette.MintWash,
    peach = MacaronPalette.Peach,
    coral = MacaronPalette.Coral,
    pink = MacaronPalette.Pink,
    pinkAccent = MacaronPalette.PinkAccent,
    blush = MacaronPalette.Blush,
    skySoft = MacaronPalette.SkySoft,
    skyAccent = MacaronPalette.SkyAccent,
    lavenderSoft = MacaronPalette.LavenderSoft,
    lavenderAccent = MacaronPalette.LavenderAccent,
    lemonSoft = MacaronPalette.LemonSoft,
    lemonAccent = MacaronPalette.LemonAccent,
    ink = MacaronPalette.Ink,
    inkMuted = MacaronPalette.InkMuted,
    border = MacaronPalette.Border,
    dogBody = MacaronPalette.DogBody,
    dogEar = MacaronPalette.DogEar,
    dogMuzzle = MacaronPalette.DogMuzzle,
    dogFeature = MacaronPalette.DogFeature,
    dogPaw = MacaronPalette.DogPaw,
    shadow = MacaronPalette.Shadow,
)

val LocalMacaronColors = staticCompositionLocalOf { LightMacaronColors }
