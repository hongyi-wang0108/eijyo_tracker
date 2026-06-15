package com.eijyo.tracker.feature.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogMascot
import com.eijyo.tracker.core.ui.component.PawPrint
import com.eijyo.tracker.core.ui.component.WelcomeBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette

private val ShadowTint = Color(0x338C5C3D)

/**
 * First Launch Welcome (PM doc §5). Introduces the app and starts profile creation;
 * deliberately offers no "just looking" entry — the only action is to begin. Layout and
 * spacing follow the "First Launch Welcome" frame in the Pencil design (393×852 reference,
 * top-anchored so element gaps stay exact across screen heights).
 */
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    val colors = EijyoTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screen),
    ) {
        WelcomeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(26.dp))
            AppLogoPill()

            Spacer(Modifier.height(46.dp))
            DogStage()

            Spacer(Modifier.height(38.dp))
            Text(
                text = stringResource(R.string.welcome_headline),
                style = EijyoTheme.typography.displaySmall,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.welcome_body),
                style = EijyoTheme.typography.bodyLarge,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(63.dp))
            StartButton(onClick = onStart)

            Spacer(Modifier.height(25.dp))
            DisclaimerPill()

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** Top brand pill: a green paw + "永住 Tracker" wordmark. */
@Composable
private fun AppLogoPill() {
    val colors = EijyoTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(42.dp)
            .shadow(8.dp, RoundedCornerShape(21.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(21.dp))
            .background(colors.card)
            .padding(start = 16.dp, end = 18.dp),
    ) {
        PawPrint(color = colors.mint, size = 16.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "永住 Tracker",
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 18.sp),
            color = colors.ink,
        )
    }
}

/** Hero "dog animation stage": rounded card with pastel blobs behind the mascot and sparkles. */
@Composable
private fun DogStage() {
    Box(
        modifier = Modifier
            .size(width = 286.dp, height = 252.dp)
            .shadow(14.dp, RoundedCornerShape(42.dp), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(42.dp))
            .background(MacaronPalette.CreamSoft),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawOval(MacaronPalette.MintContainer, Offset(34.dp.toPx(), 34.dp.toPx()), Size(104.dp.toPx(), 104.dp.toPx()))
            drawOval(MacaronPalette.Pink, Offset(166.dp.toPx(), 42.dp.toPx()), Size(82.dp.toPx(), 82.dp.toPx()))
            drawOval(MacaronPalette.SkySoft, Offset(116.dp.toPx(), 134.dp.toPx()), Size(104.dp.toPx(), 72.dp.toPx()))
        }

        DogMascot(
            size = 150.dp,
            modifier = Modifier.offset(x = 68.dp, y = 62.dp),
        )

        Sparkle("·", 26.sp, MacaronPalette.PinkAccent, 50.dp, 30.dp)
        Sparkle("·", 18.sp, MacaronPalette.LavenderAccent, 222.dp, 55.dp)
        Sparkle("·", 22.sp, MacaronPalette.SkyAccent, 220.dp, 166.dp)
        PawPrint(
            color = MacaronPalette.DogPaw,
            size = 12.dp,
            modifier = Modifier.offset(x = 235.dp, y = 28.dp),
        )
        PawPrint(
            color = MacaronPalette.DogPaw,
            size = 10.dp,
            modifier = Modifier.offset(x = 228.dp, y = 152.dp),
        )
    }
}

@Composable
private fun Sparkle(
    symbol: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
) {
    Text(
        text = symbol,
        color = color,
        style = EijyoTheme.typography.labelLarge.copy(fontSize = fontSize),
        modifier = Modifier.offset(x = x, y = y),
    )
}

/** Primary CTA: green pill with a centered label and a trailing white paw print. */
@Composable
private fun StartButton(onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color(0x3D2E855C), ambientColor = Color(0x3D2E855C))
            .clip(RoundedCornerShape(28.dp))
            .background(colors.mint)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = stringResource(R.string.welcome_start_cta),
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 17.sp),
            color = Color.White,
        )
        PawPrint(
            color = Color.White,
            size = 19.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp),
        )
    }
}

/** Bottom disclaimer carried in a soft pill, per the design. */
@Composable
private fun DisclaimerPill() {
    val colors = EijyoTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .background(colors.cardAlt)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.welcome_disclaimer),
            style = EijyoTheme.typography.labelMedium,
            color = colors.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}
