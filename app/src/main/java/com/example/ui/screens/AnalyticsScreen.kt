package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailySummary
import com.example.analytics.SalesAnalyticsReport
import com.example.ui.TimeFilter
import com.example.ui.components.MarginHealthMeter
import com.example.ui.components.ProfitGoalSimulator
import com.example.ui.components.RevenueProfitBarChart
import com.example.ui.components.StatCard
import com.example.ui.components.TimeOfDayBreakdownCard
import com.example.ui.components.TopProductsList
import com.example.ui.components.bounceClick
import com.example.ui.components.formatMoney
import com.example.ui.components.formatNumber
import com.example.ui.components.performHapticTap
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.CostAmber
import com.example.ui.theme.LossRose
import com.example.ui.theme.LossRoseBg
import com.example.ui.theme.LossRoseBorder
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBg
import com.example.ui.theme.ProfitEmeraldBorder
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    report: SalesAnalyticsReport,
    timeFilter: TimeFilter,
    selectedSpecificDateMillis: Long?,
    currencySymbol: String,
    dailyTargetProfit: Double = 100.0,
    hapticsEnabled: Boolean = true,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onSelectSpecificDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sdfDateOnly = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        if (selectedSpecificDateMillis != null) {
            cal.timeInMillis = selectedSpecificDateMillis
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSelectSpecificDate(picked.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 700.dp
        val isNarrow = maxWidth < 360.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWide) 20.dp else 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.widthIn(max = 1100.dp).fillMaxWidth()) {
                // 1. Time Filter & Specific Date Picker Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedSpecificDateMillis != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandDark)
                                    .clickable { onSelectSpecificDate(null) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sdfDateOnly.format(Date(selectedSpecificDateMillis)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SurfaceWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Specific Date",
                                        tint = SurfaceWhite,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        TimeFilter.entries.forEach { filter ->
                            val isSelected = selectedSpecificDateMillis == null && timeFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BrandDark else SurfaceWhite)
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandDark else BorderHairline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .bounceClick {
                                        performHapticTap(context, hapticsEnabled)
                                        onSelectSpecificDate(null)
                                        onTimeFilterChange(filter)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SurfaceWhite else TextPrimary
                                )
                            }
                        }
                    }

                    // Calendar Picker Button for specific date analytics
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedSpecificDateMillis != null) RevenueSapphire else SurfaceWhite)
                            .border(
                                1.dp,
                                if (selectedSpecificDateMillis != null) RevenueSapphire else BorderHairline,
                                RoundedCornerShape(12.dp)
                            )
                            .bounceClick {
                                performHapticTap(context, hapticsEnabled)
                                openDatePicker()
                            }
                            .testTag("analytics_date_picker"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick Analytics Date",
                            tint = if (selectedSpecificDateMillis != null) SurfaceWhite else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Primary KPI Cards (4-column on tablets, 2x2 on phones)
                val revCard: @Composable (Modifier) -> Unit = { cardMod ->
                    StatCard(
                        title = "Revenue",
                        value = formatMoney(report.totalRevenue, currencySymbol),
                        subValue = "${report.salesCount} units • ${report.transactionCount} sales",
                        icon = Icons.Default.Payments,
                        accentColor = RevenueSapphire,
                        modifier = cardMod
                    )
                }
                val profitCard: @Composable (Modifier) -> Unit = { cardMod ->
                    StatCard(
                        title = "Net Profit",
                        value = formatMoney(report.netProfit, currencySymbol, showPlus = true),
                        subValue = "${formatNumber(report.overallMarginPercent, 1)}% net margin",
                        icon = if (report.netProfit >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        accentColor = if (report.netProfit >= 0) ProfitEmerald else LossRose,
                        modifier = cardMod
                    )
                }
                val costCard: @Composable (Modifier) -> Unit = { cardMod ->
                    StatCard(
                        title = "Total Cost",
                        value = formatMoney(report.totalCost, currencySymbol),
                        subValue = "Cost of goods sold",
                        icon = Icons.Default.MoneyOff,
                        accentColor = CostAmber,
                        modifier = cardMod
                    )
                }
                val markupCard: @Composable (Modifier) -> Unit = { cardMod ->
                    StatCard(
                        title = "Markup Rate",
                        value = "${formatNumber(report.markupPercent, 1)}%",
                        subValue = "Return over cost",
                        icon = Icons.Default.Percent,
                        accentColor = AccentViolet,
                        modifier = cardMod
                    )
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        revCard(Modifier.weight(1f))
                        profitCard(Modifier.weight(1f))
                        costCard(Modifier.weight(1f))
                        markupCard(Modifier.weight(1f))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        revCard(Modifier.weight(1f))
                        profitCard(Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        costCard(Modifier.weight(1f))
                        markupCard(Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Efficiency Strip
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceWhite,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderHairline, RoundedCornerShape(18.dp))
                ) {
                    if (isNarrow) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                EfficiencyStat("AVG TICKET", formatMoney(report.averageTicketSize, currencySymbol), TextPrimary, Modifier.weight(1f))
                                EfficiencyStat("AVG PROFIT / SALE", formatMoney(report.averageProfitPerSale, currencySymbol, showPlus = true), if (report.averageProfitPerSale >= 0) ProfitEmerald else LossRose, Modifier.weight(1f), Alignment.End)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                EfficiencyStat("TRANSACTIONS", "${report.transactionCount}", TextPrimary, Modifier.weight(1f))
                                EfficiencyStat("UNITS SOLD", "${report.salesCount}", RevenueSapphire, Modifier.weight(1f), Alignment.End)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EfficiencyStat("AVG TICKET", formatMoney(report.averageTicketSize, currencySymbol), TextPrimary, Modifier.weight(1f))
                            EfficiencyStat("AVG PROFIT", formatMoney(report.averageProfitPerSale, currencySymbol, showPlus = true), if (report.averageProfitPerSale >= 0) ProfitEmerald else LossRose, Modifier.weight(1f), Alignment.CenterHorizontally)
                            EfficiencyStat("UNITS / SALES", "${report.salesCount} / ${report.transactionCount}", RevenueSapphire, Modifier.weight(0.9f), Alignment.End)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Charts & Health Breakdown (2-column on wide screens)
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RevenueProfitBarChart(
                            dailyData = report.dailyTrend,
                            currencySymbol = currencySymbol,
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight()
                                .testTag("analytics_trend_chart")
                        )
                        MarginHealthMeter(
                            health = report.marginHealth,
                            totalSales = report.transactionCount,
                            modifier = Modifier
                                .weight(0.85f)
                                .fillMaxHeight()
                                .testTag("margin_health_meter")
                        )
                    }
                } else {
                    RevenueProfitBarChart(
                        dailyData = report.dailyTrend,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.testTag("analytics_trend_chart")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    MarginHealthMeter(
                        health = report.marginHealth,
                        totalSales = report.transactionCount,
                        modifier = Modifier.testTag("margin_health_meter")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Time-of-Day Peak Hours + Target Profit Simulator
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TimeOfDayBreakdownCard(
                            stats = report.timeOfDayStats,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        ProfitGoalSimulator(
                            avgMarginPercent = report.overallMarginPercent,
                            currencySymbol = currencySymbol,
                            initialTargetProfit = dailyTargetProfit,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("profit_goal_simulator")
                        )
                    }
                } else {
                    TimeOfDayBreakdownCard(
                        stats = report.timeOfDayStats,
                        currencySymbol = currencySymbol
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProfitGoalSimulator(
                        avgMarginPercent = report.overallMarginPercent,
                        currencySymbol = currencySymbol,
                        initialTargetProfit = dailyTargetProfit,
                        modifier = Modifier.testTag("profit_goal_simulator")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Top Profitable & Top Revenue Products
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TopProductsList(
                            products = report.topProfitableProducts,
                            currencySymbol = currencySymbol,
                            isProfitMode = true,
                            modifier = Modifier.weight(1f).fillMaxHeight().testTag("top_profitable_products")
                        )
                        TopProductsList(
                            products = report.topRevenueProducts,
                            currencySymbol = currencySymbol,
                            isProfitMode = false,
                            modifier = Modifier.weight(1f).fillMaxHeight().testTag("top_revenue_products")
                        )
                    }
                } else {
                    TopProductsList(
                        products = report.topProfitableProducts,
                        currencySymbol = currencySymbol,
                        isProfitMode = true,
                        modifier = Modifier.testTag("top_profitable_products")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TopProductsList(
                        products = report.topRevenueProducts,
                        currencySymbol = currencySymbol,
                        isProfitMode = false,
                        modifier = Modifier.testTag("top_revenue_products")
                    )
                }

                // 7. Day-by-Day Performance Ledger
                if (report.dailyTrend.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DailyPerformanceBreakdownCard(
                        dailyTrend = report.dailyTrend.reversed(),
                        currencySymbol = currencySymbol
                    )
                }

                // 8. Best & Lowest Sale Highlights
                if (report.highestProfitSale != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val best = report.highestProfitSale
                    val worst = report.lowestProfitSale

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ProfitEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Best Performing Sale",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = best.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${formatMoney(best.profit, currencySymbol, showPlus = true)} (${formatNumber(best.marginPercent, 1)}%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitEmerald,
                                    style = TabularNumberStyle
                                )
                            }

                            if (worst != null && worst.id != best.id) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = BorderHairline
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (worst.profit < 0) LossRose else CostAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (worst.profit < 0) "Biggest Loss Transaction" else "Lowest Profit Transaction",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = worst.title,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${formatMoney(worst.profit, currencySymbol, showPlus = true)} (${formatNumber(worst.marginPercent, 1)}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (worst.profit < 0) LossRose else CostAmber,
                                        style = TabularNumberStyle
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 9. Financial Insights Card
                FinancialInsightsCard(report = report, currencySymbol = currencySymbol)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EfficiencyStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            style = TabularNumberStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DailyPerformanceBreakdownCard(
    dailyTrend: List<DailySummary>,
    currencySymbol: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BrandDark.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = BrandDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DAILY LEDGER BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            dailyTrend.take(10).forEachIndexed { idx, day ->
                val isPos = day.netProfit >= 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
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
                            text = "${day.salesCount} sales • Cost ${formatMoney(day.totalCost, currencySymbol)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatMoney(day.netProfit, currencySymbol, showPlus = true),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPos) ProfitEmerald else LossRose,
                            style = TabularNumberStyle
                        )
                        Text(
                            text = "Rev ${formatMoney(day.totalRevenue, currencySymbol)} (${formatNumber(day.marginPercent, 1)}%)",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            style = TabularNumberStyle
                        )
                    }
                }
                if (idx < dailyTrend.take(10).lastIndex) {
                    HorizontalDivider(color = BorderHairline)
                }
            }
        }
    }
}

@Composable
private fun FinancialInsightsCard(
    report: SalesAnalyticsReport,
    currencySymbol: String
) {
    val isWarning = report.marginHealth.lossCount > 0 || (report.transactionCount > 0 && report.overallMarginPercent < 15.0)
    val insightText = when {
        report.transactionCount == 0 -> "Record your store sales to unlock automatic margin optimization advice and trend analytics."
        report.marginHealth.lossCount > 0 -> "Warning: ${report.marginHealth.lossCount} sale(s) were recorded below cost. Review pricing tiers to protect net earnings."
        report.overallMarginPercent >= 35.0 -> "Strong profitability! Your ${formatNumber(report.overallMarginPercent, 1)}% margin is well above retail benchmarks. Focus on keeping top-margin items in stock."
        report.overallMarginPercent >= 20.0 -> "Healthy ${formatNumber(report.overallMarginPercent, 1)}% margin across ${report.transactionCount} sales. Upselling add-ons can lift your ${formatMoney(report.averageTicketSize, currencySymbol)} avg ticket."
        else -> "Your average margin is ${formatNumber(report.overallMarginPercent, 1)}%. Consider reviewing supplier wholesale costs or raising prices on slim-margin items."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isWarning) LossRoseBg else ProfitEmeraldBg)
            .border(1.2.dp, if (isWarning) LossRoseBorder else ProfitEmeraldBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background((if (isWarning) LossRose else ProfitEmerald).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = if (isWarning) LossRose else ProfitEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isWarning) "Margin Protection Alert" else "Store Financial Insight",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) Color(0xFF9F1239) else Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insightText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (isWarning) Color(0xFF881337) else Color(0xFF14532D)
                )
            }
        }
    }
}
