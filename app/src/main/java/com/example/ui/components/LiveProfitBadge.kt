package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    val costRatio = if (price > 0.0) (cost / price) * 100.0 else 0.0

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
                "High Margin Profit",
                Icons.Default.ArrowUpward
            )
        }
        margin >= 15.0 -> {
            Tuple5(
                Color(0xFFF0FDF4),
                Color(0xFFBBF7D0),
                Color(0xFF16A34A),
                "Healthy Return",
                Icons.Default.CheckCircle
            )
        }
        margin >= 0.0 -> {
            Tuple5(
                CostAmberBg,
                CostAmberBorder,
                CostAmber,
                "Slim Margin Alert",
                Icons.Default.Warning
            )
        }
        else -> {
            Tuple5(
                LossRoseBg,
                LossRoseBorder,
                LossRose,
                "Loss Alert",
                Icons.Default.ArrowDownward
            )
        }
    }

    val animatedBg by animateColorAsState(targetValue = bgColor, label = "badge_bg")
    val animatedBorder by animateColorAsState(targetValue = borderColor, label = "badge_border")
    val animatedText by animateColorAsState(targetValue = textColor, label = "badge_text")

    val marginFraction = ((margin.coerceIn(0.0, 60.0)) / 60.0).toFloat()
    val animatedMeterProgress by animateFloatAsState(
        targetValue = marginFraction,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "meter_progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBg)
            .border(1.5.dp, animatedBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        val isNarrow = maxWidth < 300.dp

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(animatedText.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = animatedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatMoney(profit, currencySymbol, showPlus = true),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = animatedText,
                    style = TabularNumberStyle,
                    maxLines = 1
                )
            }

            if (price > 0.0 || cost > 0.0) {
                Spacer(modifier = Modifier.height(10.dp))
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
                                        colors = listOf(CostAmber, ProfitEmerald)
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0%", fontSize = 9.sp, color = TextMuted)
                        Text("30% Healthy", fontSize = 9.sp, color = TextMuted)
                        Text("60%+ High", fontSize = 9.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceWhite.copy(alpha = 0.78f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricCell(
                    label = if (isNarrow) "MARGIN" else "PROFIT MARGIN",
                    value = "${formatNumber(margin, 1)}%",
                    valueColor = animatedText,
                    alignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "MARKUP",
                    value = "${formatNumber(markup, 1)}%",
                    valueColor = TextPrimary,
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = if (isNarrow) "COST %" else "COST RATIO",
                    value = "${formatNumber(costRatio, 1)}%",
                    valueColor = TextPrimary,
                    alignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                )
            }

            if (cost > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (profit >= 0) {
                        "Break-even: ${formatMoney(cost, currencySymbol)} • Net: ${formatMoney(profit, currencySymbol, showPlus = true)}"
                    } else {
                        "Selling ${formatMoney(-profit, currencySymbol)} below cost!"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (profit >= 0) TextSecondary else LossRose,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    valueColor: Color,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = alignment, modifier = modifier) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            style = TabularNumberStyle,
            maxLines = 1
        )
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
