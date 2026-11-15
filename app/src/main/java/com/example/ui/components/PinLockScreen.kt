package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun PinLockScreen(
    title: String = "Unlock Hisab",
    subtitle: String = "Enter your 4-digit PIN to access store sales",
    pinLength: Int = 4,
    showBiometricButton: Boolean = true,
    hapticsEnabled: Boolean = true,
    onPinComplete: (String) -> Boolean,
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

    fun onNumberPress(num: String) {
        if (currentPin.length < pinLength) {
            performHapticTap(context, hapticsEnabled, heavy = false)
            val newPin = currentPin + num
            currentPin = newPin
            errorMessage = null

            if (newPin.length == pinLength) {
                val ok = onPinComplete(newPin)
                if (!ok) {
                    performHapticTap(context, hapticsEnabled, heavy = true)
                    errorMessage = "Incorrect PIN. Try again."
                    coroutineScope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 380
                                -20f at 50
                                20f at 100
                                -14f at 150
                                14f at 200
                                -6f at 260
                                6f at 310
                                0f at 380
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
            performHapticTap(context, hapticsEnabled, heavy = false)
            currentPin = currentPin.dropLast(1)
            errorMessage = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .ambientFinancialBackdrop()
            .systemBarsPadding()
    ) {
        val isCompactHeight = maxHeight < 640.dp
        val keySize = when {
            maxHeight < 520.dp || maxWidth < 330.dp -> 54.dp
            maxHeight < 640.dp -> 60.dp
            else -> 68.dp
        }
        val rowSpacing = if (isCompactHeight) 10.dp else 14.dp
        val iconSize = if (isCompactHeight) 56.dp else 72.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.toInt(), 0) }
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(BrandDark)
                        .border(3.dp, ProfitEmerald.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize * 0.45f)
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 18.dp))

                Text(
                    text = title,
                    fontSize = if (isCompactHeight) 19.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = errorMessage ?: subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (errorMessage != null) LossRose else TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 18.dp else 26.dp))

                // Bouncy PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until pinLength) {
                        val isFilled = i < currentPin.length
                        val dotScale by animateFloatAsState(
                            targetValue = if (isFilled) 1.2f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dot_scale_$i"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = when {
                                errorMessage != null && isFilled -> LossRose
                                isFilled -> ProfitEmerald
                                else -> SurfaceSubtle
                            },
                            label = "dot_color_$i"
                        )
                        Box(
                            modifier = Modifier
                                .scale(dotScale)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    1.5.dp,
                                    if (isFilled) dotColor else BorderHairline,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 20.dp else 30.dp))

                // Numeric Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { num ->
                            KeypadButton(
                                text = num,
                                size = keySize,
                                onClick = { onNumberPress(num) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(rowSpacing))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBiometricButton && onBiometricClick != null) {
                        Surface(
                            shape = CircleShape,
                            color = ProfitEmerald.copy(alpha = 0.12f),
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .size(keySize)
                                .clip(CircleShape)
                                .border(1.5.dp, ProfitEmerald.copy(alpha = 0.45f), CircleShape)
                                .bounceClick {
                                    performHapticTap(context, hapticsEnabled)
                                    onBiometricClick()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Unlock with Biometrics",
                                    tint = ProfitEmerald,
                                    modifier = Modifier
                                        .size(keySize * 0.48f)
                                        .scale(pulseScale)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(keySize))
                    }

                    KeypadButton(
                        text = "0",
                        size = keySize,
                        onClick = { onNumberPress("0") }
                    )

                    Surface(
                        shape = CircleShape,
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(keySize)
                            .clip(CircleShape)
                            .border(1.dp, BorderHairline, CircleShape)
                            .bounceClick { onBackspace() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (onForgotPin != null) {
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 18.dp))
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
    }

    if (showForgotDialog && onForgotPin != null) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset PIN Security?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove your PIN lock and unlock the app. Your store sales data remains completely safe.") },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotDialog = false
                        onForgotPin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRose)
                ) {
                    Text("Reset PIN", color = Color.White)
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

@Composable
fun PinSetupDialog(
    hapticsEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onPinConfigured: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var step by remember { mutableIntStateOf(1) }
        var firstEnteredPin by remember { mutableStateOf("") }
        var currentPinInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val shakeOffset = remember { Animatable(0f) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        fun onNumberPress(num: String) {
            if (currentPinInput.length < 4) {
                performHapticTap(context, hapticsEnabled)
                val newPin = currentPinInput + num
                currentPinInput = newPin
                errorMessage = null

                if (newPin.length == 4) {
                    if (step == 1) {
                        firstEnteredPin = newPin
                        step = 2
                        currentPinInput = ""
                    } else {
                        if (newPin == firstEnteredPin) {
                            performHapticTap(context, hapticsEnabled)
                            onPinConfigured(newPin)
                        } else {
                            performHapticTap(context, hapticsEnabled, heavy = true)
                            errorMessage = "PINs didn't match. Please try again."
                            coroutineScope.launch {
                                shakeOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = keyframes {
                                        durationMillis = 360
                                        -18f at 50
                                        18f at 100
                                        -12f at 150
                                        12f at 200
                                        0f at 360
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
                performHapticTap(context, hapticsEnabled)
                currentPinInput = currentPinInput.dropLast(1)
                errorMessage = null
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .ambientFinancialBackdrop()
                .systemBarsPadding()
        ) {
            val isCompactHeight = maxHeight < 640.dp
            val keySize = when {
                maxHeight < 520.dp || maxWidth < 330.dp -> 54.dp
                maxHeight < 640.dp -> 60.dp
                else -> 68.dp
            }
            val rowSpacing = if (isCompactHeight) 10.dp else 14.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, BorderHairline, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close PIN Setup",
                        tint = TextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp, bottom = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .fillMaxWidth()
                            .offset { IntOffset(shakeOffset.value.toInt(), 0) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompactHeight) 56.dp else 68.dp)
                                .clip(CircleShape)
                                .background(RevenueSapphire)
                                .border(2.dp, RevenueSapphire.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompactHeight) 26.dp else 32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (step == 1) "Create 4-Digit PIN" else "Confirm Your PIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = errorMessage ?: if (step == 1) "Choose a PIN to lock your store data" else "Re-enter the same 4-digit PIN",
                            fontSize = 13.sp,
                            color = if (errorMessage != null) LossRose else TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 18.dp else 26.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                val isFilled = i < currentPinInput.length
                                val dotScale by animateFloatAsState(
                                    targetValue = if (isFilled) 1.2f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "setup_dot_$i"
                                )
                                Box(
                                    modifier = Modifier
                                        .scale(dotScale)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                errorMessage != null && isFilled -> LossRose
                                                isFilled -> RevenueSapphire
                                                else -> SurfaceSubtle
                                            }
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isFilled) RevenueSapphire else BorderHairline,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 20.dp else 30.dp))

                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9")
                        )

                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { num ->
                                    KeypadButton(
                                        text = num,
                                        size = keySize,
                                        onClick = { onNumberPress(num) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(rowSpacing))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.size(keySize))

                            KeypadButton(
                                text = "0",
                                size = keySize,
                                onClick = { onNumberPress("0") }
                            )

                            Surface(
                                shape = CircleShape,
                                color = SurfaceWhite,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(keySize)
                                    .clip(CircleShape)
                                    .border(1.dp, BorderHairline, CircleShape)
                                    .bounceClick { onBackspace() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    size: Dp,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, BorderHairline, CircleShape)
            .bounceClick(scaleDown = 0.90f, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (size < 58.dp) 20.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
