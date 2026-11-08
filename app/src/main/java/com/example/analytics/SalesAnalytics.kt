package com.example.analytics

import com.example.model.SaleEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ProductStat(
    val title: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val avgMarginPercent: Double
)

data class DailySummary(
    val dateLabel: String,
    val dateTimestamp: Long,
    val totalRevenue: Double,
    val totalCost: Double,
    val netProfit: Double,
    val marginPercent: Double,
    val salesCount: Int
)

data class MarginHealthBreakdown(
    val highMarginCount: Int, // > 35%
    val moderateMarginCount: Int, // 15% - 35%
    val lowMarginCount: Int, // 0% - 15%
    val lossCount: Int // < 0%
)

data class SalesAnalyticsReport(
    val totalRevenue: Double,
    val totalCost: Double,
    val netProfit: Double,
    val overallMarginPercent: Double,
    val markupPercent: Double,
    val salesCount: Int,
    val averageTicketSize: Double,
    val averageProfitPerSale: Double,
    val topProfitableProducts: List<ProductStat>,
    val topRevenueProducts: List<ProductStat>,
    val dailyTrend: List<DailySummary>,
    val marginHealth: MarginHealthBreakdown,
    val highestProfitSale: SaleEntity?,
    val lowestProfitSale: SaleEntity?
)

object AnalyticsEngine {

    fun generateReport(sales: List<SaleEntity>): SalesAnalyticsReport {
        if (sales.isEmpty()) {
            return SalesAnalyticsReport(
                totalRevenue = 0.0,
                totalCost = 0.0,
                netProfit = 0.0,
                overallMarginPercent = 0.0,
                markupPercent = 0.0,
                salesCount = 0,
                averageTicketSize = 0.0,
                averageProfitPerSale = 0.0,
                topProfitableProducts = emptyList(),
                topRevenueProducts = emptyList(),
                dailyTrend = emptyList(),
                marginHealth = MarginHealthBreakdown(0, 0, 0, 0),
                highestProfitSale = null,
                lowestProfitSale = null
            )
        }

        val totalRev = sales.sumOf { it.price * it.quantity }
        val totalCost = sales.sumOf { it.cost * it.quantity }
        val netProfit = totalRev - totalCost
        val count = sales.sumOf { it.quantity }
        val overallMargin = if (totalRev > 0) (netProfit / totalRev) * 100.0 else 0.0
        val markup = if (totalCost > 0) (netProfit / totalCost) * 100.0 else 0.0
        val avgTicket = if (count > 0) totalRev / count else 0.0
        val avgProfit = if (count > 0) netProfit / count else 0.0

        // Margin health
        var high = 0
        var mod = 0
        var low = 0
        var loss = 0
        sales.forEach { s ->
            when {
                s.marginPercent >= 35.0 -> high++
                s.marginPercent >= 15.0 -> mod++
                s.marginPercent >= 0.0 -> low++
                else -> loss++
            }
        }

        // Product stats
        val grouped = sales.groupBy { it.title.trim().lowercase(Locale.ROOT) }
        val productStats = grouped.map { (_, items) ->
            val title = items.first().title
            val units = items.sumOf { it.quantity }
            val rev = items.sumOf { it.price * it.quantity }
            val cost = items.sumOf { it.cost * it.quantity }
            val prof = rev - cost
            val margin = if (rev > 0) (prof / rev) * 100.0 else 0.0
            ProductStat(
                title = title,
                unitsSold = units,
                totalRevenue = rev,
                totalCost = cost,
                totalProfit = prof,
                avgMarginPercent = margin
            )
        }

        val topProfitable = productStats.sortedByDescending { it.totalProfit }.take(5)
        val topRevenue = productStats.sortedByDescending { it.totalRevenue }.take(5)

        // Daily trend
        val sdf = SimpleDateFormat("MMM dd", Locale.US)
        val dayGrouped = sales.groupBy { s ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = s.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }

        val dailyTrend = dayGrouped.map { (dayMillis, daySales) ->
            val dRev = daySales.sumOf { it.price * it.quantity }
            val dCost = daySales.sumOf { it.cost * it.quantity }
            val dProf = dRev - dCost
            val dMargin = if (dRev > 0) (dProf / dRev) * 100.0 else 0.0
            DailySummary(
                dateLabel = sdf.format(Date(dayMillis)),
                dateTimestamp = dayMillis,
                totalRevenue = dRev,
                totalCost = dCost,
                netProfit = dProf,
                marginPercent = dMargin,
                salesCount = daySales.sumOf { it.quantity }
            )
        }.sortedBy { it.dateTimestamp }

        val highestSale = sales.maxByOrNull { it.profit }
        val lowestSale = sales.minByOrNull { it.profit }

        return SalesAnalyticsReport(
            totalRevenue = totalRev,
            totalCost = totalCost,
            netProfit = netProfit,
            overallMarginPercent = overallMargin,
            markupPercent = markup,
            salesCount = count,
            averageTicketSize = avgTicket,
            averageProfitPerSale = avgProfit,
            topProfitableProducts = topProfitable,
            topRevenueProducts = topRevenue,
            dailyTrend = dailyTrend,
            marginHealth = MarginHealthBreakdown(high, mod, low, loss),
            highestProfitSale = highestSale,
            lowestProfitSale = lowestSale
        )
    }

    fun isToday(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isInLastDays(timestamp: Long, days: Int): Boolean {
        val limit = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        return timestamp >= limit
    }
}
