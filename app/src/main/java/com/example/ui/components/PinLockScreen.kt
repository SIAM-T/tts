package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandDark
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun PinLockScreen(
    title: String = "Unlock Hisab",
    subtitle: String = "Enter your 4-digit PIN to access store sales",
    pinLength: Int = 4,
    showBiometricButton: Boolean = true,
    onPinComplete: (String) -> Boolean, // returns true if success
    onBiometricClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    fun vibrate(type: Int = 0) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (type == 1) {
                VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
            } else {
                VibrationEffect.createOneShot(25, 80)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (type == 1) 120 else 25)
        }
    }

    fun onNumberPress(num: String) {
        if (currentPin.length < pinLength) {
            vibrate(0)
            val newPin = currentPin + num
            currentPin = newPin
            errorMessage = null

            if (newPin.length == pinLength) {
                val ok = onPinComplete(newPin)
                if (!ok) {
                    vibrate(1)
                    errorMessage = "Incorrect PIN. Try again."
                    coroutineScope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -20f at 50
                                20f at 100
                                -15f at 150
                                15f at 200
                                -8f at 250
                                8f at 300
                                0f at 400
                            }
                        )
                        currentPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (currentPin.isNotEmpty()) {
            vibrate(0)
            currentPin = currentPin.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.toInt(), 0) }
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BrandDark)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = errorMessage ?: subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (errorMessage != null) LossRose else TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until pinLength) {
                    val isFilled = i < currentPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) {
                                    if (errorMessage != null) LossRose else ProfitEmerald
                                } else SurfaceSubtle
                            )
                            .border(
                                1.5.dp,
                                if (isFilled) ProfitEmerald else BorderSubtle,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Numeric Keypad (1 to 9)
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { num ->
                        KeypadButton(
                            text = num,
                            onClick = { onNumberPress(num) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bottom Row: Biometric, 0, Backspace
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBiometricButton && onBiometricClick != null) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBiometricClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            tint = ProfitEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(68.dp))
                }

                KeypadButton(
                    text = "0",
                    onClick = { onNumberPress("0") }
                )

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { onBackspace() }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
