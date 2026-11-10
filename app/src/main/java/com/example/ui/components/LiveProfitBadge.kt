package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CostAmber
import com.example.ui.theme.CostAmberBg
import com.example.ui.theme.CostAmberBorder
import com.example.ui.theme.LossRose
import com.example.ui.theme.LossRoseBg
import com.example.ui.theme.LossRoseBorder
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBg
import com.example.ui.theme.ProfitEmeraldBorder
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.RevenueSapphireBg
import com.example.ui.theme.RevenueSapphireBorder
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun LiveProfitBadge(
    price: Double,
    cost: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val profit = price - cost
    val margin = if (price > 0.0) (profit / price) * 100.0 else 0.0
    val markup = if (cost > 0.0) (profit / cost) * 100.0 else 0.0

    val (bgColor, borderColor, textColor, badgeTitle, icon) = when {
        price <= 0.0 && cost <= 0.0 -> {
            Tuple5(
                RevenueSapphireBg,
                RevenueSapphireBorder,
                RevenueSapphire,
                "Ready to Calculate",
                Icons.Default.Bolt
            )
        }
        margin >= 35.0 -> {
            Tuple5(
                ProfitEmeraldBg,
                ProfitEmeraldBorder,
                ProfitEmerald,
                "High Margin Profit 🚀",
                Icons.Default.ArrowUpward
            )
        }
        margin >= 15.0 -> {
            Tuple5(
                Color(0xFFF0FDF4),
                Color(0xFFBBF7D0),
                Color(0xFF16A34A),
                "Healthy Return 👍",
                Icons.Default.CheckCircle
            )
        }
        margin >= 0.0 -> {
            Tuple5(
                CostAmberBg,
                CostAmberBorder,
                CostAmber,
                "Slim Margin Alert ⚠️",
                Icons.Default.Warning
            )
        }
        else -> {
            Tuple5(
                LossRoseBg,
                LossRoseBorder,
                LossRose,
                "Loss Alert 🚨",
                Icons.Default.ArrowDownward
            )
        }
    }

    val animatedBg by animateColorAsState(targetValue = bgColor, label = "bg")
    val animatedBorder by animateColorAsState(targetValue = borderColor, label = "border")

    // Animated margin fraction for spectrum meter (range 0f to 1f, clamped between 0% and 60%)
    val marginFraction = ((margin.coerceIn(0.0, 60.0)) / 60.0).toFloat()
    val animatedMeterProgress by animateFloatAsState(
        targetValue = marginFraction,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "meter_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBg)
            .border(1.5.dp, animatedBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Text(
                    text = if (profit >= 0) "+$currencySymbol${String.format(Locale.US, "%.2f", profit)}"
                    else "-$currencySymbol${String.format(Locale.US, "%.2f", -profit)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Visual Margin Spectrum Gauge
            if (price > 0.0 || cost > 0.0) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedMeterProgress.coerceAtLeast(0.02f))
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            CostAmber,
                                            ProfitEmerald
                                        )
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0% (Break-even)", fontSize = 9.sp, color = TextMuted)
                        Text("30% (Healthy)", fontSize = 9.sp, color = TextMuted)
                        Text("60%+ (Outstanding)", fontSize = 9.sp, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3 Key Financial Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceWhite.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PROFIT MARGIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", margin)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MARKUP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", markup)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "COST RATIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = TextSecondary
                    )
                    val costRatio = if (price > 0.0) (cost / price) * 100.0 else 0.0
                    Text(
                        text = "${String.format(Locale.US, "%.1f", costRatio)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Break-even guidance note
            if (cost > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (profit >= 0) "Break-even price: $currencySymbol${String.format(Locale.US, "%.2f", cost)} • Net return: $currencySymbol${String.format(Locale.US, "%.2f", profit)}"
                        else "Loss Warning: Price is $currencySymbol${String.format(Locale.US, "%.2f", -profit)} below product cost!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (profit >= 0) TextSecondary else LossRose
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
