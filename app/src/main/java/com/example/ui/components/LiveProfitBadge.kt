package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
                "Negative Profit / Loss 🚨",
                Icons.Default.ArrowDownward
            )
        }
    }

    val animatedBg by animateColorAsState(targetValue = bgColor, label = "bg")
    val animatedBorder by animateColorAsState(targetValue = borderColor, label = "border")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBg)
            .border(1.5.dp, animatedBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizallySafe()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
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
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }

                Text(
                    text = if (profit >= 0) "+$currencySymbol${String.format(Locale.US, "%.2f", profit)}"
                    else "-$currencySymbol${String.format(Locale.US, "%.2f", -profit)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PROFIT MARGIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", margin)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MARKUP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", markup)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "COST RATIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSecondary
                    )
                    val costRatio = if (price > 0.0) (cost / price) * 100.0 else 0.0
                    Text(
                        text = "${String.format(Locale.US, "%.1f", costRatio)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

private fun Alignment.Companion.CenterVertizallySafe(): Alignment.Vertical = Alignment.CenterVertically

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
