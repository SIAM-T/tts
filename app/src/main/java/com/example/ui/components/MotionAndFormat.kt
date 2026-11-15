package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.TabularNumberStyle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private val usSymbols = DecimalFormatSymbols(Locale.US)
private val moneyFormatter = DecimalFormat("#,##0.00", usSymbols)

fun formatMoney(amount: Double, currencySymbol: String, showPlus: Boolean = false): String {
    val absFormatted = moneyFormatter.format(abs(amount))
    return when {
        amount < -0.0001 -> "-$currencySymbol$absFormatted"
        showPlus && amount > 0.0001 -> "+$currencySymbol$absFormatted"
        else -> "$currencySymbol$absFormatted"
    }
}

fun formatNumber(value: Double, decimals: Int = 1): String {
    return String.format(Locale.US, "%.${decimals}f", value)
}

fun performHapticTap(context: Context, enabled: Boolean, heavy: Boolean = false) {
    if (!enabled) return
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val duration = if (heavy) 65L else 18L
            val amp = if (heavy) VibrationEffect.DEFAULT_AMPLITUDE else 65
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amp))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (heavy) 65L else 18L)
        }
    } catch (_: Exception) { }
}

/**
 * Springy tactile press effect for cards, chips, and buttons.
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce_scale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Subtle atmospheric light theme gradient backdrop for financial clarity.
 */
fun Modifier.ambientFinancialBackdrop(): Modifier = this.drawBehind {
    drawRect(color = BackgroundLight)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF10B981).copy(alpha = 0.07f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.85f, size.height * 0.08f),
            radius = size.maxDimension * 0.45f
        )
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2563EB).copy(alpha = 0.06f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.12f, size.height * 0.16f),
            radius = size.maxDimension * 0.40f
        )
    )
}

/**
 * Smoothly animated numeric money readout using tabular monospace figures.
 */
@Composable
fun AnimatedMoneyText(
    targetAmount: Double,
    currencySymbol: String,
    showPlus: Boolean = false,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    modifier: Modifier = Modifier,
    style: TextStyle = TabularNumberStyle
) {
    val animatedVal by animateFloatAsState(
        targetValue = targetAmount.toFloat(),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "money_roll"
    )
    Text(
        text = formatMoney(animatedVal.toDouble(), currencySymbol, showPlus),
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
