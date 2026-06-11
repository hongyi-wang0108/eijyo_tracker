package com.eijyo.tracker.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eijyo.tracker.core.ui.theme.MacaronPalette

/**
 * Full-bleed decorative background for the First Launch Welcome screen, mirroring the
 * "First Launch Welcome" frame in the Pencil design: four soft macaron bubbles bleeding
 * off the corners, plus faint scattered paw prints and dog bones. Coordinates follow the
 * design's 393×852 reference frame and scale to the actual canvas.
 */
@Composable
fun WelcomeBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val sx = size.width / 393f
        val sy = size.height / 852f
        // Diameter scale: keep bubbles/paws round by scaling by the smaller axis.
        val s = minOf(sx, sy)

        fun bubble(x: Float, y: Float, d: Float, color: Color) {
            drawOval(color = color, topLeft = Offset(x * sx, y * sy), size = Size(d * s, d * s))
        }
        bubble(-78f, -54f, 210f, MacaronPalette.MintSoft)
        bubble(292f, 48f, 152f, MacaronPalette.Peach)
        bubble(250f, 604f, 190f, MacaronPalette.LavenderSoft)
        bubble(-44f, 650f, 145f, MacaronPalette.LemonSoft)

        // Scattered paw prints (design "welcome paw print").
        data class Paw(val x: Float, val y: Float, val rot: Float, val color: Color)
        listOf(
            Paw(46f, 136f, -18f, MacaronPalette.DogPaw.copy(alpha = 0.11f)),
            Paw(316f, 210f, 16f, MacaronPalette.Coral.copy(alpha = 0.11f)),
            Paw(54f, 486f, 14f, MacaronPalette.SkyAccent.copy(alpha = 0.09f)),
            Paw(310f, 706f, -12f, MacaronPalette.DogPaw.copy(alpha = 0.10f)),
        ).forEach { paw ->
            val left = paw.x * sx
            val top = paw.y * sy
            val w = 19f * s
            val h = 15f * s
            rotate(paw.rot, pivot = Offset(left + w / 2f, top + h / 2f)) {
                drawPawInRect(left, top, w, h, paw.color)
            }
        }

        // Scattered dog bones (design "welcome dog bone").
        data class Bone(val x: Float, val y: Float, val rot: Float, val color: Color)
        listOf(
            Bone(284f, 122f, -14f, MacaronPalette.LemonAccent.copy(alpha = 0.12f)),
            Bone(36f, 710f, 18f, MacaronPalette.Coral.copy(alpha = 0.10f)),
        ).forEach { bone ->
            val left = bone.x * sx
            val top = bone.y * sy
            val w = 21f * s
            val h = 10f * s
            rotate(bone.rot, pivot = Offset(left + w / 2f, top + h / 2f)) {
                drawBoneInRect(left, top, w, h, bone.color)
            }
        }
    }
}

/**
 * Full-bleed decorative background for the Onboarding screens, mirroring the
 * "Onboarding Dog Background Layer": three corner bubbles (no lemon, unlike welcome)
 * plus faint scattered paw prints and bones. Coordinates follow the 393×852 reference.
 */
@Composable
fun OnboardingBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val sx = size.width / 393f
        val sy = size.height / 852f
        val s = minOf(sx, sy)

        fun bubble(x: Float, y: Float, d: Float, color: Color) {
            drawOval(color = color, topLeft = Offset(x * sx, y * sy), size = Size(d * s, d * s))
        }
        bubble(-62f, -48f, 182f, MacaronPalette.MintSoft)
        bubble(304f, 58f, 126f, MacaronPalette.Peach)
        bubble(285f, 640f, 148f, MacaronPalette.LavenderSoft)

        data class Paw(val x: Float, val y: Float, val rot: Float, val color: Color)
        listOf(
            Paw(92f, 120f, -14f, MacaronPalette.DogPaw.copy(alpha = 0.10f)),
            Paw(312f, 270f, 18f, MacaronPalette.DogPaw.copy(alpha = 0.12f)),
            Paw(30f, 404f, -10f, MacaronPalette.Coral.copy(alpha = 0.10f)),
            Paw(326f, 506f, 16f, MacaronPalette.SkyAccent.copy(alpha = 0.09f)),
            Paw(42f, 560f, -18f, MacaronPalette.DogPaw.copy(alpha = 0.10f)),
            Paw(70f, 694f, 12f, MacaronPalette.DogPaw.copy(alpha = 0.10f)),
        ).forEach { paw ->
            val left = paw.x * sx
            val top = paw.y * sy
            val w = 15f * s
            val h = 12f * s
            rotate(paw.rot, pivot = Offset(left + w / 2f, top + h / 2f)) {
                drawPawInRect(left, top, w, h, paw.color)
            }
        }

        data class Bone(val x: Float, val y: Float, val rot: Float, val color: Color)
        listOf(
            Bone(278f, 116f, -12f, MacaronPalette.LemonAccent.copy(alpha = 0.12f)),
            Bone(38f, 238f, 16f, MacaronPalette.Coral.copy(alpha = 0.10f)),
            Bone(304f, 642f, -10f, MacaronPalette.PinkAccent.copy(alpha = 0.09f)),
        ).forEach { bone ->
            val left = bone.x * sx
            val top = bone.y * sy
            val w = 18f * s
            val h = 9f * s
            rotate(bone.rot, pivot = Offset(left + w / 2f, top + h / 2f)) {
                drawBoneInRect(left, top, w, h, bone.color)
            }
        }
    }
}

/** A single solid paw print, sized to fill its box. Used inline on the CTA and logo pill. */
@Composable
fun PawPrint(color: Color, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Canvas(modifier = modifier.size(size)) {
        drawPawInRect(0f, 0f, this.size.width, this.size.height, color)
    }
}

/**
 * Faint scattered paw prints for use as a generic background on inner screens.
 * Kept separate from [WelcomeBackground], which also carries the corner bubbles.
 */
@Composable
fun PawPrintBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val spots = listOf(
            Triple(0.12f, 0.16f, -18f) to MacaronPalette.DogPaw,
            Triple(0.82f, 0.24f, 16f) to MacaronPalette.Coral,
            Triple(0.16f, 0.58f, 14f) to MacaronPalette.SkyAccent,
            Triple(0.84f, 0.82f, -12f) to MacaronPalette.DogPaw,
            Triple(0.30f, 0.88f, 10f) to MacaronPalette.LavenderAccent,
        )
        spots.forEach { (pos, color) ->
            val (fx, fy, angle) = pos
            val w = 22f
            val left = size.width * fx - w / 2f
            val top = size.height * fy - w * 0.4f
            rotate(angle, pivot = Offset(size.width * fx, size.height * fy)) {
                drawPawInRect(left, top, w, w * 0.8f, color.copy(alpha = 0.10f))
            }
        }
    }
}

/** Draws a paw (pad + four toes) filling the given rect, normalized to the design proportions. */
private fun DrawScope.drawPawInRect(left: Float, top: Float, w: Float, h: Float, color: Color) {
    drawOval(
        color = color,
        topLeft = Offset(left + 0.236f * w, top + 0.485f * h),
        size = Size(0.471f * w, 0.448f * h),
    )
    val toes = listOf(
        0.029f to 0.224f,
        0.236f to 0.037f,
        0.501f to 0.037f,
        0.707f to 0.224f,
    )
    toes.forEach { (tx, ty) ->
        drawOval(
            color = color,
            topLeft = Offset(left + tx * w, top + ty * h),
            size = Size(0.206f * w, 0.261f * h),
        )
    }
}

/** Draws a dog bone (two rounded ends + middle bar) filling the given rect. */
private fun DrawScope.drawBoneInRect(left: Float, top: Float, w: Float, h: Float, color: Color) {
    val endW = 0.29f * w
    val endH = 0.61f * h
    val ends = listOf(
        0f to 0f,
        0f to 0.389f,
        0.711f to 0f,
        0.711f to 0.389f,
    )
    ends.forEach { (ex, ey) ->
        drawOval(
            color = color,
            topLeft = Offset(left + ex * w, top + ey * h),
            size = Size(endW, endH),
        )
    }
    drawOval(
        color = color,
        topLeft = Offset(left + 0.184f * w, top + 0.278f * h),
        size = Size(0.632f * w, 0.444f * h),
    )
}
