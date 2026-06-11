package com.eijyo.tracker.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eijyo.tracker.core.ui.theme.MacaronPalette

/**
 * The brand mascot drawn with Canvas primitives, mirroring the proportions of the
 * "Hero dog mascot" group in the Pencil design. [wagging] adds the looping tail wag
 * called for in the welcome-screen animation notes.
 */
@Composable
fun DogMascot(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    wagging: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "dog")
    val tailAngle by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tail",
    )

    Canvas(modifier = modifier.size(width = size, height = size)) {
        drawDog(if (wagging) tailAngle else 0f)
    }
}

/**
 * Just the dog's face (head, ears, eyes, nose, blush) — used inside the small helper
 * bubble on the onboarding screens, where the full body doesn't fit. Mirrors the
 * "dog helper face" group in the Pencil design.
 */
@Composable
fun DogFace(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        // Reference box 48.6 x 43.2; normalize into the available square.
        fun ovalAt(cx: Float, cy: Float, ew: Float, eh: Float, color: androidx.compose.ui.graphics.Color) {
            val k = s / 48.6f
            drawOval(
                color = color,
                topLeft = Offset((cx - ew / 2f) * k, (cy - eh / 2f) * k),
                size = Size(ew * k, eh * k),
            )
        }
        // Draw order matches the design's "dog helper face": face first, then ears on
        // top (so the brown ears read as clear flaps over the head, not slivers), then
        // muzzle / blush / eyes / nose.
        ovalAt(24.3f, 21.15f, 32.4f, 27.9f, MacaronPalette.DogBody)
        ovalAt(8.55f, 21.15f, 11.7f, 18.9f, MacaronPalette.DogEar)
        ovalAt(40.05f, 21.15f, 11.7f, 18.9f, MacaronPalette.DogEar)
        ovalAt(24.3f, 27f, 14.4f, 9f, MacaronPalette.DogMuzzle)
        ovalAt(14.85f, 27f, 6.3f, 3.6f, MacaronPalette.Blush)
        ovalAt(34.65f, 27f, 6.3f, 3.6f, MacaronPalette.Blush)
        ovalAt(18.9f, 19.35f, 3.6f, 4.5f, MacaronPalette.DogFeature)
        ovalAt(29.7f, 19.35f, 3.6f, 4.5f, MacaronPalette.DogFeature)
        ovalAt(24.75f, 27f, 4.5f, 3.6f, MacaronPalette.DogFeature)
    }
}

/**
 * A larger, smiling dog face (with rotated ears and a little mouth) used on the
 * "Generated stage card" of the onboarding-complete screen. Mirrors the design's
 * "completion dog face" group (86×78 reference box).
 */
@Composable
fun DogFaceSmiling(modifier: Modifier = Modifier, size: Dp = 86.dp) {
    Canvas(modifier = modifier.size(size)) {
        val k = this.size.minDimension / 86f
        fun ovalAt(cx: Float, cy: Float, ew: Float, eh: Float, deg: Float, color: androidx.compose.ui.graphics.Color) {
            val draw = {
                drawOval(
                    color = color,
                    topLeft = Offset((cx - ew / 2f) * k, (cy - eh / 2f) * k),
                    size = Size(ew * k, eh * k),
                )
            }
            if (deg == 0f) draw() else rotate(deg, pivot = Offset(cx * k, cy * k)) { draw() }
        }
        fun mouth(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = MacaronPalette.DogFeature,
                start = Offset(x1 * k, y1 * k),
                end = Offset(x2 * k, y2 * k),
                strokeWidth = 1.7f * k,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        ovalAt(44f, 37f, 58f, 50f, 0f, MacaronPalette.DogBody)
        ovalAt(15f, 41f, 22f, 38f, -10f, MacaronPalette.DogEar)
        ovalAt(72f, 41f, 22f, 38f, 10f, MacaronPalette.DogEar)
        ovalAt(43.5f, 49f, 25f, 16f, 0f, MacaronPalette.DogMuzzle)
        ovalAt(27f, 48f, 12f, 6f, 0f, MacaronPalette.Blush)
        ovalAt(61f, 48f, 12f, 6f, 0f, MacaronPalette.Blush)
        ovalAt(34f, 35.5f, 6f, 7f, 0f, MacaronPalette.DogFeature)
        ovalAt(53f, 35.5f, 6f, 7f, 0f, MacaronPalette.DogFeature)
        ovalAt(44f, 50f, 8f, 6f, 0f, MacaronPalette.DogFeature)
        mouth(37f, 55f, 45f, 61f)
        mouth(47f, 55f, 55f, 61f)
    }
}

private fun DrawScope.drawDog(tailAngle: Float) {
    val w = size.width
    val h = size.height
    // Reference design box is 150 x 142; scale into the available size.
    val sx = w / 150f
    val sy = h / 142f
    fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
    fun ovalAt(cx: Float, cy: Float, ew: Float, eh: Float, color: androidx.compose.ui.graphics.Color) {
        drawOval(
            color = color,
            topLeft = p(cx - ew / 2f, cy - eh / 2f),
            size = Size(ew * sx, eh * sy),
        )
    }

    // Tail (drawn first, behind body), points down-right and wags around its base.
    rotate(degrees = tailAngle, pivot = p(108f, 99f)) {
        drawLine(
            color = MacaronPalette.DogPaw,
            start = p(108f, 99f),
            end = p(138f, 121f),
            strokeWidth = 8f * sx,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }

    // Ears (slightly splayed outward, matching the design's ±14° tilt).
    rotate(degrees = -14f, pivot = p(33f, 63f)) {
        ovalAt(33f, 63f, 30f, 54f, MacaronPalette.DogEar)
    }
    rotate(degrees = 14f, pivot = p(116f, 63f)) {
        ovalAt(116f, 63f, 30f, 54f, MacaronPalette.DogEar)
    }
    // Body & head
    ovalAt(76f, 104f, 66f, 48f, MacaronPalette.DogBody)
    ovalAt(75f, 59f, 82f, 70f, MacaronPalette.DogBody)
    // Muzzle
    ovalAt(76f, 71f, 36f, 23f, MacaronPalette.DogMuzzle)
    // Blush
    ovalAt(48f, 68f, 16f, 8f, MacaronPalette.Blush)
    ovalAt(104f, 68f, 16f, 8f, MacaronPalette.Blush)
    // Eyes
    ovalAt(59f, 53f, 8f, 9f, MacaronPalette.DogFeature)
    ovalAt(92f, 53f, 8f, 9f, MacaronPalette.DogFeature)
    // Nose
    ovalAt(77f, 71f, 11f, 8f, MacaronPalette.DogFeature)
    // Paws
    ovalAt(58f, 125f, 18f, 15f, MacaronPalette.DogMuzzle)
    ovalAt(94f, 125f, 18f, 15f, MacaronPalette.DogMuzzle)
}
