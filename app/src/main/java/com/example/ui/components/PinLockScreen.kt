package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.RevenueSapphire
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
    onForgotPin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    fun vibrate(type: Int = 0) {
        try {
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
        } catch (_: Exception) { }
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

    // Biometric glowing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEEF2F6)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.toInt(), 0) }
        ) {
            // Icon with soft glowing ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(BrandDark)
                    .border(3.dp, ProfitEmerald.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

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

            // PIN Dots Indicator with bouncy animated dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until pinLength) {
                    val isFilled = i < currentPin.length
                    val dotScale = if (isFilled) 1.15f else 1f
                    Box(
                        modifier = Modifier
                            .scale(dotScale)
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

            Spacer(modifier = Modifier.height(32.dp))

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
                            .background(ProfitEmerald.copy(alpha = 0.1f))
                            .border(1.5.dp, ProfitEmerald.copy(alpha = 0.4f), CircleShape)
                            .clickable(onClick = onBiometricClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            tint = ProfitEmerald,
                            modifier = Modifier
                                .size(34.dp)
                                .scale(pulseScale)
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
                        .background(SurfaceWhite)
                        .border(1.2.dp, BorderSubtle, CircleShape)
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

            // Forgot PIN Reset action
            if (onForgotPin != null) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = { showForgotDialog = true }) {
                    Text(
                        text = "Forgot PIN? Reset Security",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RevenueSapphire
                    )
                }
            }
        }
    }

    if (showForgotDialog && onForgotPin != null) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset PIN Security?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove your existing PIN and unlock the app. Your sales data will remain safe.") },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotDialog = false
                        onForgotPin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRose)
                ) {
                    Text("Yes, Reset PIN", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Dedicated, pristine Full-Screen PIN Setup / Change Composable.
 * Solves the step 1 -> step 2 memory bug and provides a gorgeous UI.
 */
@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinConfigured: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: choose, 2: confirm
    var firstEnteredPin by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun vibrate(type: Int = 0) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (type == 1) {
                    VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(25, 80)
                }
                vibrator.vibrate(effect)
            }
        } catch (_: Exception) { }
    }

    fun onNumberPress(num: String) {
        if (currentPinInput.length < 4) {
            vibrate(0)
            val newPin = currentPinInput + num
            currentPinInput = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (step == 1) {
                    // Advance to Step 2
                    firstEnteredPin = newPin
                    step = 2
                    currentPinInput = "" // PROPERLY CLEARED!
                } else {
                    // Compare with Step 1
                    if (newPin == firstEnteredPin) {
                        vibrate(0)
                        onPinConfigured(newPin)
                    } else {
                        // Mismatch!
                        vibrate(1)
                        errorMessage = "PINs didn't match. Choose again."
                        coroutineScope.launch {
                            shakeOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = keyframes {
                                    durationMillis = 350
                                    -18f at 50
                                    18f at 100
                                    -12f at 150
                                    12f at 200
                                    0f at 350
                                }
                            )
                            step = 1
                            firstEnteredPin = ""
                            currentPinInput = ""
                        }
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (currentPinInput.isNotEmpty()) {
            vibrate(0)
            currentPinInput = currentPinInput.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Top Cancel Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceSubtle)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.toInt(), 0) }
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(RevenueSapphire)
                    .border(2.dp, RevenueSapphire.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RevenueSapphire.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (step == 1) "STEP 1 OF 2" else "STEP 2 OF 2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RevenueSapphire
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (step == 1) "Create 4-Digit PIN" else "Confirm Your PIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = errorMessage ?: if (step == 1) "Choose a secure PIN for your store" else "Re-enter the same 4-digit PIN",
                fontSize = 13.sp,
                color = if (errorMessage != null) LossRose else TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < currentPinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) {
                                    if (errorMessage != null) LossRose else RevenueSapphire
                                } else SurfaceSubtle
                            )
                            .border(
                                1.5.dp,
                                if (isFilled) RevenueSapphire else BorderSubtle,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
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

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(68.dp))

                KeypadButton(
                    text = "0",
                    onClick = { onNumberPress("0") }
                )

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.2.dp, BorderSubtle, CircleShape)
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
