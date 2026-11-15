package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailySummary
import com.example.analytics.MarginHealthBreakdown
import com.example.analytics.ProductStat
import com.example.analytics.TimeOfDayStat
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CostAmber
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBright
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.RevenueSapphireBright
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun RevenueProfitBarChart(
    dailyData: List<DailySummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            shadowElevation = 2.dp,
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "No sales in selected period to chart",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
        return
    }

    var selectedIndex by remember(dailyData) { mutableStateOf<Int?>(null) }

    val maxVal = remember(dailyData) {
        val maxRev = dailyData.maxOfOrNull { it.totalRevenue } ?: 1.0
        val maxProf = dailyData.maxOfOrNull { abs(it.netProfit) } ?: 1.0
        maxOf(maxRev, maxProf, 10.0) * 1.15
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyData) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    val density = LocalDensity.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "REVENUE VS PROFIT TREND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Tap any day bar for exact breakdown",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RevenueSapphire))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rev", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProfitEmerald))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profit", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }

            // Interactive Day Details Callout
            val selectedDay = selectedIndex?.let { dailyData.getOrNull(it) }
            AnimatedVisibility(
                visible = selectedDay != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedDay?.let { day ->
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = day.dateLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${day.salesCount} sales",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = formatMoney(day.totalRevenue, currencySymbol),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RevenueSapphire,
                                            style = TabularNumberStyle
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("PROFIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = formatMoney(day.netProfit, currencySymbol, showPlus = true),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (day.netProfit >= 0) ProfitEmerald else LossRose,
                                            style = TabularNumberStyle
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("MARGIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = "${formatNumber(day.marginPercent, 1)}%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (day.netProfit >= 0) ProfitEmerald else LossRose,
                                            style = TabularNumberStyle
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Responsive Chart Canvas area (expands to fill wide containers, scrolls for many days)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val minSlotWidth = 64.dp
                val desiredWidth = minSlotWidth * dailyData.size
                val chartWidth = if (desiredWidth > maxWidth) desiredWidth else maxWidth
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(chartWidth)
                            .height(190.dp)
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
                        val h = size.height - 34.dp.toPx()
                        val slotWidth = w / dailyData.size
                        val barWidth = (slotWidth * 0.24f).coerceIn(10.dp.toPx(), 20.dp.toPx())
                        val spacing = 4.dp.toPx()

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

                            if (selectedIndex == index) {
                                drawRoundRect(
                                    color = Color(0xFFEFF6FF),
                                    topLeft = Offset(centerX - slotWidth / 2f + 4f, 0f),
                                    size = Size(slotWidth - 8f, h),
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                )
                            }

                            // Revenue Bar with gradient
                            val revHeight = ((item.totalRevenue / maxVal) * h * progress).toFloat().coerceAtLeast(3f)
                            val revLeft = centerX - barWidth - spacing / 2f
                            val revTop = h - revHeight
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(RevenueSapphireBright, RevenueSapphire),
                                    startY = revTop,
                                    endY = h
                                ),
                                topLeft = Offset(revLeft, revTop),
                                size = Size(barWidth, revHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // Profit Bar with gradient
                            val profVal = item.netProfit
                            val isLoss = profVal < 0
                            val profHeight = ((abs(profVal) / maxVal) * h * progress).toFloat().coerceAtLeast(3f)
                            val profLeft = centerX + spacing / 2f
                            val profTop = if (isLoss) h - profHeight else (h - profHeight)
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = if (isLoss) listOf(LossRose, Color(0xFFBE123C)) else listOf(ProfitEmeraldBright, ProfitEmerald),
                                    startY = profTop,
                                    endY = h
                                ),
                                topLeft = Offset(profLeft, profTop),
                                size = Size(barWidth, profHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (selectedIndex == index) {
                                        android.graphics.Color.parseColor("#0F172A")
                                    } else {
                                        android.graphics.Color.parseColor("#64748B")
                                    }
                                    textSize = with(density) { 10.sp.toPx() }
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = (selectedIndex == index)
                                    isAntiAlias = true
                                }
                                drawText(item.dateLabel, centerX, h + 21.dp.toPx(), paint)
                            }
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
    val sweepAnim = remember { Animatable(0f) }
    LaunchedEffect(health, totalSales) {
        sweepAnim.snapTo(0f)
        sweepAnim.animateTo(1f, animationSpec = tween(750, easing = FastOutSlowInEasing))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MARGIN HEALTH DISTRIBUTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (totalSales == 0) {
                Text("No transactions recorded in this period", fontSize = 12.sp, color = TextMuted)
                return@Column
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 330.dp

                val donutContent: @Composable () -> Unit = {
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(84.dp)) {
                            val strokeW = 12.dp.toPx()
                            val progress = sweepAnim.value
                            var currentAngle = -90f

                            drawCircle(
                                color = SurfaceSubtle,
                                radius = (size.minDimension - strokeW) / 2f,
                                style = Stroke(strokeW)
                            )

                            val highAngle = (health.highMarginCount.toFloat() / totalSales) * 360f * progress
                            val modAngle = (health.moderateMarginCount.toFloat() / totalSales) * 360f * progress
                            val lowAngle = (health.lowMarginCount.toFloat() / totalSales) * 360f * progress
                            val lossAngle = (health.lossCount.toFloat() / totalSales) * 360f * progress

                            if (highAngle > 0f) {
                                drawArc(color = ProfitEmerald, startAngle = currentAngle, sweepAngle = highAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Butt))
                                currentAngle += highAngle
                            }
                            if (modAngle > 0f) {
                                drawArc(color = RevenueSapphire, startAngle = currentAngle, sweepAngle = modAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Butt))
                                currentAngle += modAngle
                            }
                            if (lowAngle > 0f) {
                                drawArc(color = CostAmber, startAngle = currentAngle, sweepAngle = lowAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Butt))
                                currentAngle += lowAngle
                            }
                            if (lossAngle > 0f) {
                                drawArc(color = LossRose, startAngle = currentAngle, sweepAngle = lossAngle, useCenter = false, style = Stroke(strokeW, cap = StrokeCap.Butt))
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalSales",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                style = TabularNumberStyle
                            )
                            Text(
                                text = "SALES",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMuted
                            )
                        }
                    }
                }

                val legendContent: @Composable (Modifier) -> Unit = { legModifier ->
                    Column(
                        modifier = legModifier,
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        MarginLegendRow("High (>35%)", health.highMarginCount, totalSales, ProfitEmerald)
                        MarginLegendRow("Healthy (15-35%)", health.moderateMarginCount, totalSales, RevenueSapphire)
                        MarginLegendRow("Slim (0-15%)", health.lowMarginCount, totalSales, CostAmber)
                        MarginLegendRow("Loss (<0%)", health.lossCount, totalSales, LossRose)
                    }
                }

                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        donutContent()
                        legendContent(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        donutContent()
                        Spacer(modifier = Modifier.width(16.dp))
                        legendContent(Modifier.weight(1f))
                    }
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count (${formatNumber(pct, 0)}%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            style = TabularNumberStyle
        )
    }
}

@Composable
fun TimeOfDayBreakdownCard(
    stats: List<TimeOfDayStat>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(RevenueSapphire.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = RevenueSapphire,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PEAK SALES HOURS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Revenue & profit by time of day",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (stats.isEmpty() || stats.all { it.count == 0 }) {
                Text("No time-of-day data for this period", fontSize = 12.sp, color = TextMuted)
                return@Column
            }

            val maxRev = stats.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
            val peakPeriod = stats.maxByOrNull { it.revenue }?.periodLabel

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.forEach { slot ->
                    val fraction by animateFloatAsState(
                        targetValue = (slot.revenue / maxRev).toFloat().coerceIn(0f, 1f),
                        animationSpec = tween(550, easing = FastOutSlowInEasing),
                        label = "tod_${slot.periodLabel}"
                    )
                    val isPeak = slot.periodLabel == peakPeriod && slot.revenue > 0

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
                                Text(
                                    text = slot.periodLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isPeak) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ProfitEmerald.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PEAK", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ProfitEmerald)
                                    }
                                }
                            }
                            Text(
                                text = "${slot.count} • ${formatMoney(slot.revenue, currencySymbol)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPeak) ProfitEmerald else TextSecondary,
                                style = TabularNumberStyle
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction.coerceAtLeast(if (slot.count > 0) 0.04f else 0f))
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPeak) ProfitEmerald else RevenueSapphire)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Target Profit & Break-Even Simulator
 */
@Composable
fun ProfitGoalSimulator(
    avgMarginPercent: Double,
    currencySymbol: String,
    initialTargetProfit: Double = 200.0,
    modifier: Modifier = Modifier
) {
    var targetProfit by remember(initialTargetProfit) {
        mutableDoubleStateOf(initialTargetProfit.coerceIn(50.0, 2000.0))
    }

    val safeMargin = if (avgMarginPercent > 5.0) avgMarginPercent else 30.0
    val requiredRevenue = if (safeMargin > 0) (targetProfit / (safeMargin / 100.0)) else 0.0

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${formatMoney(targetProfit, currencySymbol)}/day",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RevenueSapphire,
                    style = TabularNumberStyle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = targetProfit.toFloat(),
                onValueChange = { targetProfit = (it / 10).toInt() * 10.0 },
                valueRange = 50f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = RevenueSapphire,
                    activeTrackColor = RevenueSapphire
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("REQUIRED SALES REVENUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatMoney(requiredRevenue, currencySymbol),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            style = TabularNumberStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("MARGIN BASE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${formatNumber(safeMargin, 1)}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfitEmerald,
                            style = TabularNumberStyle
                        )
                    }
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isProfitMode) "TOP PROFITABLE PRODUCTS" else "TOP REVENUE DRIVERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
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
                    val targetProgress = if (maxMetric > 0) (metric / maxMetric).toFloat().coerceIn(0.05f, 1f) else 0.05f
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(550, easing = FastOutSlowInEasing),
                        label = "prod_bar_${product.title}"
                    )

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
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (index) {
                                                0 -> Color(0xFFFEF3C7)
                                                1 -> Color(0xFFF1F5F9)
                                                2 -> Color(0xFFFFEDD5)
                                                else -> SurfaceSubtle
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when (index) {
                                            0 -> Color(0xFFB45309)
                                            1 -> Color(0xFF475569)
                                            2 -> Color(0xFFC2410C)
                                            else -> TextMuted
                                        },
                                        style = TabularNumberStyle
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = product.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatMoney(metric, currencySymbol),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfitMode) ProfitEmerald else RevenueSapphire,
                                style = TabularNumberStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SurfaceSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isProfitMode) ProfitEmerald else RevenueSapphire)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${product.unitsSold} units sold",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Margin: ${formatNumber(product.avgMarginPercent, 1)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                style = TabularNumberStyle
                            )
                        }
                    }
                }
            }
        }
    }
}
