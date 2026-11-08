package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailySummary
import com.example.analytics.MarginHealthBreakdown
import com.example.analytics.ProductStat
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CostAmber
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RevenueProfitBarChart(
    dailyData: List<DailySummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No sales in selected period to chart",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
        return
    }

    val maxVal = remember(dailyData) {
        val maxRev = dailyData.maxOfOrNull { it.totalRevenue } ?: 1.0
        val maxProf = dailyData.maxOfOrNull { it.netProfit } ?: 1.0
        maxOf(maxRev, maxProf, 10.0) * 1.15
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyData) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REVENUE VS PROFIT TREND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = TextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RevenueSapphire))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revenue", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProfitEmerald))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profit", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart area
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                val itemWidth = 56.dp
                val totalChartWidth = (itemWidth * dailyData.size.coerceAtLeast(5)).coerceAtLeast(300.dp)

                Canvas(
                    modifier = Modifier
                        .width(totalChartWidth)
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height - 35.dp.toPx() // leave room for labels
                    val slotWidth = w / dailyData.size
                    val barWidth = 14.dp.toPx()
                    val spacing = 3.dp.toPx()

                    // Grid lines
                    val steps = 3
                    for (i in 0..steps) {
                        val y = h - (h / steps) * i
                        drawLine(
                            color = Color(0xFFF1F5F9),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    dailyData.forEachIndexed { index, item ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val progress = animationProgress.value

                        // Revenue Bar
                        val revHeight = ((item.totalRevenue / maxVal) * h * progress).toFloat()
                        val revLeft = centerX - barWidth - spacing / 2f
                        val revTop = h - revHeight
                        drawRoundRect(
                            color = RevenueSapphire,
                            topLeft = Offset(revLeft, revTop),
                            size = Size(barWidth, revHeight.coerceAtLeast(3f)),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Profit Bar (could be negative if loss)
                        val profVal = item.netProfit
                        val isLoss = profVal < 0
                        val profColor = if (isLoss) LossRose else ProfitEmerald
                        val profHeight = ((Math.abs(profVal) / maxVal) * h * progress).toFloat()
                        val profLeft = centerX + spacing / 2f
                        val profTop = if (isLoss) h else (h - profHeight)
                        drawRoundRect(
                            color = profColor,
                            topLeft = Offset(profLeft, profTop),
                            size = Size(barWidth, profHeight.coerceAtLeast(3f)),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Draw Date Label below
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#64748B")
                                textSize = with(density) { 10.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(item.dateLabel, centerX, h + 22.dp.toPx(), paint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarginHealthMeter(
    health: MarginHealthBreakdown,
    totalSales: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "MARGIN HEALTH DISTRIBUTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (totalSales == 0) {
                Text("No sales data available", fontSize = 12.sp, color = TextMuted)
                return@Column
            }

            // Horizontal Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceSubtle)
            ) {
                if (health.highMarginCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(health.highMarginCount.toFloat())
                            .height(16.dp)
                            .background(ProfitEmerald)
                    )
                }
                if (health.moderateMarginCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(health.moderateMarginCount.toFloat())
                            .height(16.dp)
                            .background(RevenueSapphire)
                    )
                }
                if (health.lowMarginCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(health.lowMarginCount.toFloat())
                            .height(16.dp)
                            .background(CostAmber)
                    )
                }
                if (health.lossCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(health.lossCount.toFloat())
                            .height(16.dp)
                            .background(LossRose)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend with counts and percentages
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MarginLegendRow(
                    label = "High Margin (>35%)",
                    count = health.highMarginCount,
                    total = totalSales,
                    color = ProfitEmerald
                )
                MarginLegendRow(
                    label = "Moderate Margin (15% - 35%)",
                    count = health.moderateMarginCount,
                    total = totalSales,
                    color = RevenueSapphire
                )
                MarginLegendRow(
                    label = "Low Margin (0% - 15%)",
                    count = health.lowMarginCount,
                    total = totalSales,
                    color = CostAmber
                )
                MarginLegendRow(
                    label = "Loss (<0%)",
                    count = health.lossCount,
                    total = totalSales,
                    color = LossRose
                )
            }
        }
    }
}

@Composable
private fun MarginLegendRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val pct = if (total > 0) (count.toDouble() / total) * 100.0 else 0.0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = TextPrimary)
        }
        Text(
            text = "$count (${String.format(Locale.US, "%.0f", pct)}%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    }
}

@Composable
fun TopProductsList(
    products: List<ProductStat>,
    currencySymbol: String,
    isProfitMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = if (isProfitMode) "TOP PROFITABLE PRODUCTS" else "TOP REVENUE DRIVERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (products.isEmpty()) {
                Text("No items recorded yet", fontSize = 12.sp, color = TextMuted)
                return@Column
            }

            val maxMetric = products.maxOfOrNull { if (isProfitMode) it.totalProfit else it.totalRevenue } ?: 1.0

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                products.forEachIndexed { index, product ->
                    val metric = if (isProfitMode) product.totalProfit else product.totalRevenue
                    val progress = if (maxMetric > 0) (metric / maxMetric).toFloat().coerceIn(0.05f, 1f) else 0.05f

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = product.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", metric)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfitMode) ProfitEmerald else RevenueSapphire
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SurfaceSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(6.dp)
                                    .background(if (isProfitMode) ProfitEmerald else RevenueSapphire)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${product.unitsSold} units sold",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Avg Margin: ${String.format(Locale.US, "%.1f", product.avgMarginPercent)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
