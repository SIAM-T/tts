package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailySummary
import com.example.analytics.MarginHealthBreakdown
import com.example.analytics.ProductStat
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandDark
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
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceWhite)
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
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

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

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
                Column {
                    Text(
                        text = "REVENUE VS PROFIT TREND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Tap any day bar for exact details",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

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

            // Interactive Day Details Callout
            val selectedDay = selectedIndex?.let { dailyData.getOrNull(it) }
            AnimatedVisibility(visible = selectedDay != null) {
                selectedDay?.let { day ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = day.dateLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${day.salesCount} transactions",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text("$currencySymbol${String.format(Locale.US, "%.2f", day.totalRevenue)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RevenueSapphire)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PROFIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text("${if (day.netProfit >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", day.netProfit)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (day.netProfit >= 0) ProfitEmerald else LossRose)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("MARGIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text("${String.format(Locale.US, "%.1f", day.marginPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (day.netProfit >= 0) ProfitEmerald else LossRose)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Canvas area with Horizontal Scroll
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                val itemWidth = 58.dp
                val totalChartWidth = (itemWidth * dailyData.size.coerceAtLeast(5)).coerceAtLeast(300.dp)

                Canvas(
                    modifier = Modifier
                        .width(totalChartWidth)
                        .height(180.dp)
                        .pointerInput(dailyData) {
                            detectTapGestures { offset ->
                                val slotW = size.width / dailyData.size
                                val clickedIdx = (offset.x / slotW).toInt()
                                if (clickedIdx in dailyData.indices) {
                                    selectedIndex = if (selectedIndex == clickedIdx) null else clickedIdx
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height - 35.dp.toPx()
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

                        // Highlighting tapped slot
                        if (selectedIndex == index) {
                            drawRoundRect(
                                color = Color(0xFFF8FAFC),
                                topLeft = Offset(centerX - slotWidth / 2f + 2f, 0f),
                                size = Size(slotWidth - 4f, h),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

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

                        // Profit Bar (positive or negative)
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
                                color = if (selectedIndex == index) android.graphics.Color.parseColor("#0F172A") else android.graphics.Color.parseColor("#64748B")
                                textSize = with(density) { 10.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = (selectedIndex == index)
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

            Spacer(modifier = Modifier.height(14.dp))

            if (totalSales == 0) {
                Text("No sales data available", fontSize = 12.sp, color = TextMuted)
                return@Column
            }

            // Donut chart representation + Segmented Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Arc Donut Chart
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeW = 12.dp.toPx()
                        var currentAngle = -90f

                        val highAngle = (health.highMarginCount.toFloat() / totalSales) * 360f
                        val modAngle = (health.moderateMarginCount.toFloat() / totalSales) * 360f
                        val lowAngle = (health.lowMarginCount.toFloat() / totalSales) * 360f
                        val lossAngle = (health.lossCount.toFloat() / totalSales) * 360f

                        if (highAngle > 0) {
                            drawArc(color = ProfitEmerald, startAngle = currentAngle, sweepAngle = highAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round))
                            currentAngle += highAngle
                        }
                        if (modAngle > 0) {
                            drawArc(color = RevenueSapphire, startAngle = currentAngle, sweepAngle = modAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round))
                            currentAngle += modAngle
                        }
                        if (lowAngle > 0) {
                            drawArc(color = CostAmber, startAngle = currentAngle, sweepAngle = lowAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round))
                            currentAngle += lowAngle
                        }
                        if (lossAngle > 0) {
                            drawArc(color = LossRose, startAngle = currentAngle, sweepAngle = lossAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Round))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalSales",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "SALES",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Breakdown list
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MarginLegendRow(
                        label = "High Margin (>35%)",
                        count = health.highMarginCount,
                        total = totalSales,
                        color = ProfitEmerald
                    )
                    MarginLegendRow(
                        label = "Healthy (15% - 35%)",
                        count = health.moderateMarginCount,
                        total = totalSales,
                        color = RevenueSapphire
                    )
                    MarginLegendRow(
                        label = "Slim (0% - 15%)",
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
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.sp, color = TextPrimary)
        }
        Text(
            text = "$count (${String.format(Locale.US, "%.0f", pct)}%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}

/**
 * Interactive Target Profit & Break-Even Simulator
 */
@Composable
fun ProfitGoalSimulator(
    avgMarginPercent: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var targetProfit by remember { mutableDoubleStateOf(200.0) }

    val safeMargin = if (avgMarginPercent > 5.0) avgMarginPercent else 30.0
    val requiredRevenue = if (safeMargin > 0) (targetProfit / (safeMargin / 100.0)) else 0.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(RevenueSapphire.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = RevenueSapphire,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TARGET PROFIT SIMULATOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "Goal: $currencySymbol${targetProfit.toInt()}/day",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RevenueSapphire
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Slider(
                value = targetProfit.toFloat(),
                onValueChange = { targetProfit = (it / 10).toInt() * 10.0 },
                valueRange = 50f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = RevenueSapphire,
                    activeTrackColor = RevenueSapphire
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("REQUIRED SALES REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", requiredRevenue)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STORE MARGIN BASE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", safeMargin)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfitEmerald
                    )
                }
            }
        }
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

            val rankBadges = listOf("🥇", "🥈", "🥉", "4.", "5.")

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
                                    text = rankBadges.getOrElse(index) { "${index + 1}." },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
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

                        // Animated Progress Bar
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
