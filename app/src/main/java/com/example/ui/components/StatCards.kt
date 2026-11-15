package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TodaySummary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar
import java.util.Locale

@Composable
fun StatCard(
    title: String,
    value: String,
    subValue: String? = null,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color = SurfaceWhite,
    borderColor: Color = BorderHairline,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = 2.dp,
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(Locale.US),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                label = "stat_value"
            ) { targetVal ->
                Text(
                    text = targetVal,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    style = TabularNumberStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (subValue != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subValue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DailyEarningsSummaryHeader(
    summary: TodaySummary,
    currencySymbol: String,
    storeName: String = "My Store",
    dailyTargetProfit: Double = 100.0,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    val goalFraction = if (dailyTargetProfit > 0) {
        (summary.profit / dailyTargetProfit).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val goalAchieved = summary.profit >= dailyTargetProfit && dailyTargetProfit > 0

    val animatedGoalProgress by animateFloatAsState(
        targetValue = goalFraction,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "goal_progress"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B192C),
                            Color(0xFF173252),
                            Color(0xFF112A46)
                        )
                    )
                )
                .border(1.dp, Color(0xFF2B4666), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column {
                // Top Row: Store Greeting & Live Count
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
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (goalAchieved) Color(0xFFFBBF24) else ProfitEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$greeting • $storeName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.09f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${summary.count} units • ${summary.transactionCount} sales",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA7F3D0),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Profit Readout + Goal Ring (weight(1f) prevents text overlapping ring on narrow phones)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Net Profit",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AnimatedMoneyText(
                            targetAmount = summary.profit,
                            currencySymbol = currencySymbol,
                            showPlus = true,
                            color = if (summary.profit >= 0) Color(0xFF34D399) else Color(0xFFFB7185),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rev: ${formatMoney(summary.revenue, currencySymbol)}",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                style = TabularNumberStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Margin: ${formatNumber(summary.marginPercent, 1)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7),
                                style = TabularNumberStyle,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Daily Profit Goal Progress Ring
                    val ringColor = if (goalAchieved) Color(0xFFFBBF24) else Color(0xFF10B981)
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(64.dp)) {
                            val strokeW = 5.5.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.14f),
                                radius = (size.minDimension - strokeW) / 2,
                                style = Stroke(strokeW)
                            )
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedGoalProgress,
                                useCenter = false,
                                style = Stroke(strokeW, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (goalAchieved) "100%" else "${(goalFraction * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (goalAchieved) Color(0xFFFDE68A) else Color.White,
                                style = TabularNumberStyle
                            )
                            Text(
                                text = if (goalAchieved) "HIT!" else "GOAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (goalAchieved) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
