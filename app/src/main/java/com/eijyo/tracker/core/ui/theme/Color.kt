package com.eijyo.tracker.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Macaron palette extracted from the Pencil design (`pencil-welcome-desktop.pen`).
 * Material3's [androidx.compose.material3.ColorScheme] only has a fixed set of slots,
 * so the extra brand/semantic tones (peach, sky, lavender, lemon, dog colors) live
 * here and are exposed to screens through [MacaronColors] / LocalMacaronColors.
 */
object MacaronPalette {
    // Surfaces & background
    val Cream = Color(0xFFFFF6EC)        // screen background
    val CreamSoft = Color(0xFFFFFCF7)    // raised card surface
    val CreamCard = Color(0xFFFFFDF8)    // alternate card surface
    val White = Color(0xFFFFFFFF)

    // Mint (primary / positive / progress / main button)
    val Mint = Color(0xFF35A978)
    val MintContainer = Color(0xFFB8F2D3)
    val MintSoft = Color(0xFFD9F7E8)
    val MintWash = Color(0xFFE4FAEF)     // very light mint used for selected/active fills

    // Peach / pink (reminders, warmth, decoration)
    val Peach = Color(0xFFFFE1D9)
    val Coral = Color(0xFFEA826C)
    val Pink = Color(0xFFFFDCEB)
    val PinkAccent = Color(0xFFDB6A9A)
    val Blush = Color(0xFFFFB8C7)

    // Sky blue (information)
    val SkySoft = Color(0xFFD8F0FF)
    val SkyAccent = Color(0xFF4A9ED8)

    // Lavender (auxiliary)
    val LavenderSoft = Color(0xFFECE7FF)
    val LavenderAccent = Color(0xFF7464D8)

    // Lemon (light reminders / warnings)
    val LemonSoft = Color(0xFFFFF4C8)
    val LemonAccent = Color(0xFFB98C12)

    // Text
    val Ink = Color(0xFF3A302B)          // primary text
    val InkMuted = Color(0xFF8A7B72)     // secondary text
    val Border = Color(0xFFEFE3D6)

    // Dog mascot
    val DogBody = Color(0xFFF4C99D)
    val DogEar = Color(0xFFB87449)
    val DogMuzzle = Color(0xFFFFF6E9)
    val DogFeature = Color(0xFF8B573B)
    val DogPaw = Color(0xFFD8945D)

    // Brown-tinted shadow used throughout the design
    val Shadow = Color(0x1F8C5C3D)
}
