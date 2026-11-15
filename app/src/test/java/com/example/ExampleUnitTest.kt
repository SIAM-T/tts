package com.example

import com.example.analytics.AnalyticsEngine
import com.example.model.SaleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testProfitAndMarginCalculations() {
        val sales = listOf(
            SaleEntity(id = 1, title = "Wireless Mouse", price = 100.0, cost = 60.0, quantity = 2), // Rev 200, Cost 120, Profit 80
            SaleEntity(id = 2, title = "USB Cable", price = 50.0, cost = 25.0, quantity = 1)        // Rev 50, Cost 25, Profit 25
        )
        val report = AnalyticsEngine.generateReport(sales)

        assertEquals(250.0, report.totalRevenue, 0.001)
        assertEquals(145.0, report.totalCost, 0.001)
        assertEquals(105.0, report.netProfit, 0.001)
        assertEquals(42.0, report.overallMarginPercent, 0.001) // 105 / 250 * 100
        assertEquals(3, report.salesCount)
        assertEquals(2, report.transactionCount)
        assertEquals(125.0, report.averageTicketSize, 0.01)
        assertEquals(52.5, report.averageProfitPerSale, 0.01)
    }

    @Test
    fun testLossMarginCalculation() {
        val sales = listOf(
            SaleEntity(id = 1, title = "Clearance Item", price = 30.0, cost = 50.0, quantity = 1)
        )
        val report = AnalyticsEngine.generateReport(sales)
        assertEquals(-20.0, report.netProfit, 0.001)
        assertTrue(report.overallMarginPercent < 0)
        assertEquals(1, report.marginHealth.lossCount)
    }

    @Test
    fun testTopProfitableProductRankingAndTimeOfDayStats() {
        val morningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }
        val eveningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
        }

        val sales = listOf(
            SaleEntity(id = 1, title = "Keyboard", price = 150.0, cost = 80.0, quantity = 1, timestamp = morningCal.timeInMillis),
            SaleEntity(id = 2, title = "Mousepad", price = 20.0, cost = 5.0, quantity = 2, timestamp = eveningCal.timeInMillis)
        )
        val report = AnalyticsEngine.generateReport(sales)
        assertEquals("Keyboard", report.topProfitableProducts.first().title)
        assertEquals(70.0, report.topProfitableProducts.first().totalProfit, 0.001)
        assertEquals(4, report.timeOfDayStats.size)
        assertEquals(150.0, report.timeOfDayStats.first { it.periodLabel.startsWith("Morning") }.revenue, 0.001)
        assertEquals(40.0, report.timeOfDayStats.first { it.periodLabel.startsWith("Evening") }.revenue, 0.001)
    }
}
