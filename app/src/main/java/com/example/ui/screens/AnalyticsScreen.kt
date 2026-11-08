package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.SalesAnalyticsReport
import com.example.ui.TimeFilter
import com.example.ui.components.MarginHealthMeter
import com.example.ui.components.RevenueProfitBarChart
import com.example.ui.components.StatCard
import com.example.ui.components.TopProductsList
import com.example.ui.theme.BackgroundLight
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
fun AnalyticsScreen(
    report: SalesAnalyticsReport,
    timeFilter: TimeFilter,
    currencySymbol: String,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Time Filter Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeFilter.entries.forEach { filter ->
                    val isSelected = timeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BrandDark else SurfaceWhite)
                            .border(
                                1.dp,
                                if (isSelected) BrandDark else BorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onTimeFilterChange(filter) }
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Primary KPI Grid (2x2 Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Revenue",
                    value = "$currencySymbol${String.format(Locale.US, "%.2f", report.totalRevenue)}",
                    subValue = "${report.salesCount} items sold",
                    icon = Icons.Default.Payments,
                    accentColor = RevenueSapphire,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Net Profit",
                    value = "${if (report.netProfit >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", report.netProfit)}",
                    subValue = "${String.format(Locale.US, "%.1f", report.overallMarginPercent)}% net margin",
                    icon = if (report.netProfit >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    accentColor = if (report.netProfit >= 0) ProfitEmerald else LossRose,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Cost",
                    value = "$currencySymbol${String.format(Locale.US, "%.2f", report.totalCost)}",
                    subValue = "Cost of Goods Sold",
                    icon = Icons.Default.MoneyOff,
                    accentColor = CostAmber,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Markup Rate",
                    value = "${String.format(Locale.US, "%.1f", report.markupPercent)}%",
                    subValue = "Avg over cost",
                    icon = Icons.Default.Percent,
                    accentColor = Color(0xFF8B5CF6), // Purple
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Efficiency Metrics Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AVG TICKET SIZE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", report.averageTicketSize)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AVG PROFIT / SALE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", report.averageProfitPerSale)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.averageProfitPerSale >= 0) ProfitEmerald else LossRose
                        )
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL UNITS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${report.salesCount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Revenue vs Profit Daily Trend Bar Chart
            RevenueProfitBarChart(
                dailyData = report.dailyTrend,
                currencySymbol = currencySymbol,
                modifier = Modifier.testTag("analytics_trend_chart")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Margin Health Distribution Meter
            MarginHealthMeter(
                health = report.marginHealth,
                totalSales = report.salesCount,
                modifier = Modifier.testTag("margin_health_meter")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Top Profitable Products
            TopProductsList(
                products = report.topProfitableProducts,
                currencySymbol = currencySymbol,
                isProfitMode = true,
                modifier = Modifier.testTag("top_profitable_products")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Top Revenue Drivers
            TopProductsList(
                products = report.topRevenueProducts,
                currencySymbol = currencySymbol,
                isProfitMode = false,
                modifier = Modifier.testTag("top_revenue_products")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Highlights (Best & Lowest sale)
            if (report.highestProfitSale != null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Highest Profit Transaction",
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
                                text = report.highestProfitSale.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "+$currencySymbol${String.format(Locale.US, "%.2f", report.highestProfitSale.profit)} (${String.format(Locale.US, "%.1f", report.highestProfitSale.marginPercent)}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfitEmerald
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 9. Financial Insights Card
            FinancialInsightsCard(report = report)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FinancialInsightsCard(report: SalesAnalyticsReport) {
    val insightText = when {
        report.salesCount == 0 -> "Record your store sales to unlock automatic margin optimization advice and trend analytics."
        report.overallMarginPercent >= 35.0 -> "Outstanding financial performance! Your profit margin is ${String.format(Locale.US, "%.1f", report.overallMarginPercent)}%, significantly above average retail margins. Keep pushing high-margin inventory."
        report.overallMarginPercent >= 20.0 -> "Solid profit health with ${String.format(Locale.US, "%.1f", report.overallMarginPercent)}% margin. Consider bundling accessories to increase your average ticket size ($${String.format(Locale.US, "%.2f", report.averageTicketSize)})."
        report.marginHealth.lossCount > 0 -> "Warning: You have ${report.marginHealth.lossCount} transaction(s) sold below cost. Review your pricing tiers to prevent margin erosion."
        else -> "Your store margin is ${String.format(Locale.US, "%.1f", report.overallMarginPercent)}%. Consider reviewing supplier wholesale costs to capture higher net returns."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.2.dp, Color(0xFFBBF7D0), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ProfitEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = ProfitEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Store Financial Insight",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insightText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF14532D)
                )
            }
        }
    }
}
