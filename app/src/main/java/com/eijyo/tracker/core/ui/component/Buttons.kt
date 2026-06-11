package com.eijyo.tracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.EijyoTokens

/** Primary mint pill button used for the main action on every screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(EijyoTokens.RadiusButton.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EijyoTheme.colors.mint,
            contentColor = EijyoTheme.colors.card,
            disabledContainerColor = EijyoTheme.colors.mintContainer,
            disabledContentColor = EijyoTheme.colors.inkMuted,
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        Text(text = text, style = EijyoTheme.typography.labelLarge)
    }
}

/**
 * Brand primary button: a green pill with a centered label and a trailing white paw
 * print, matching the CTAs throughout the Pencil design (Welcome / onboarding / complete).
 */
@Composable
fun PawButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    cornerRadius: Dp = 28.dp,
) {
    val colors = EijyoTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(12.dp, RoundedCornerShape(cornerRadius), spotColor = Color(0x3D2E855C), ambientColor = Color(0x3D2E855C))
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (enabled) colors.mint else colors.mintContainer)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text = text,
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 17.sp),
            color = if (enabled) Color.White else colors.inkMuted,
        )
        if (enabled) {
            PawPrint(
                color = Color.White,
                size = 19.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
            )
        }
    }
}

/** Secondary, low-emphasis action (e.g. "返回上一题"). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(EijyoTokens.RadiusButton.dp),
    ) {
        Text(text = text, style = EijyoTheme.typography.labelLarge, color = EijyoTheme.colors.inkMuted)
    }
}
