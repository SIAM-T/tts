package com.example.data

import com.example.model.SaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleRepository(private val saleDao: SaleDao) {

    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val recentTitles: Flow<List<String>> = saleDao.getRecentTitles()

    suspend fun insertSale(sale: SaleEntity): Long = withContext(Dispatchers.IO) {
        saleDao.insertSale(sale)
    }

    suspend fun updateSale(sale: SaleEntity) = withContext(Dispatchers.IO) {
        saleDao.updateSale(sale)
    }

    suspend fun deleteSale(sale: SaleEntity) = withContext(Dispatchers.IO) {
        saleDao.deleteSale(sale)
    }

    suspend fun deleteSaleById(id: Long) = withContext(Dispatchers.IO) {
        saleDao.deleteSaleById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        saleDao.clearAllSales()
    }

    suspend fun exportToCsv(sales: List<SaleEntity>): String = withContext(Dispatchers.Default) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Price,Cost,Profit,MarginPercent,Quantity,Note\n")
        sales.forEach { s ->
            val escapedTitle = "\"" + s.title.replace("\"", "\"\"") + "\""
            val escapedNote = "\"" + s.note.replace("\"", "\"\"") + "\""
            val dateStr = sdf.format(Date(s.timestamp))
            sb.append("${s.id},${dateStr},${escapedTitle},${s.price},${s.cost},${s.profit},${String.format(Locale.US, "%.2f", s.marginPercent)},${s.quantity},${escapedNote}\n")
        }
        sb.toString()
    }

    suspend fun exportToJson(sales: List<SaleEntity>): String = withContext(Dispatchers.Default) {
        val jsonArray = JSONArray()
        sales.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("price", s.price)
                put("cost", s.cost)
                put("profit", s.profit)
                put("marginPercent", s.marginPercent)
                put("timestamp", s.timestamp)
                put("quantity", s.quantity)
                put("note", s.note)
            }
            jsonArray.put(obj)
        }
        jsonArray.toString(2)
    }

    suspend fun importFromCsv(csvContent: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return@runCatching 0

            val itemsToInsert = mutableListOf<SaleEntity>()
            // Skip header if first line contains title/price
            val startIdx = if (lines[0].contains("Price", ignoreCase = true) || lines[0].contains("Title", ignoreCase = true)) 1 else 0

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            for (i in startIdx until lines.size) {
                val line = lines[i]
                val tokens = parseCsvLine(line)
                if (tokens.size >= 4) {
                    val title = tokens.getOrNull(2) ?: tokens.getOrNull(0) ?: "Item"
                    val price = tokens.getOrNull(3)?.toDoubleOrNull() ?: tokens.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    val cost = tokens.getOrNull(4)?.toDoubleOrNull() ?: tokens.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    val qty = tokens.getOrNull(7)?.toIntOrNull() ?: 1
                    val note = tokens.getOrNull(8) ?: ""
                    val time = tokens.getOrNull(1)?.let {
                        try { sdf.parse(it)?.time } catch (_: Exception) { null }
                    } ?: System.currentTimeMillis()

                    itemsToInsert.add(
                        SaleEntity(
                            id = 0, // Generate new auto ID
                            title = title,
                            price = price,
                            cost = cost,
                            profit = price - cost,
                            marginPercent = if (price > 0) ((price - cost) / price) * 100.0 else 0.0,
                            timestamp = time,
                            quantity = qty,
                            note = note
                        )
                    )
                }
            }

            if (itemsToInsert.isNotEmpty()) {
                saleDao.insertSales(itemsToInsert)
            }
            itemsToInsert.size
        }
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val array = JSONArray(jsonString)
            val itemsToInsert = mutableListOf<SaleEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Item")
                val price = obj.optDouble("price", 0.0)
                val cost = obj.optDouble("cost", 0.0)
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val quantity = obj.optInt("quantity", 1)
                val note = obj.optString("note", "")

                itemsToInsert.add(
                    SaleEntity(
                        id = 0,
                        title = title,
                        price = price,
                        cost = cost,
                        profit = price - cost,
                        marginPercent = if (price > 0) ((price - cost) / price) * 100.0 else 0.0,
                        timestamp = timestamp,
                        quantity = quantity,
                        note = note
                    )
                )
            }
            if (itemsToInsert.isNotEmpty()) {
                saleDao.insertSales(itemsToInsert)
            }
            itemsToInsert.size
        }
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        val samples = listOf(
            SaleEntity(title = "Wireless Ergonomic Mouse", price = 34.99, cost = 16.50, timestamp = now - (oneHour * 1)),
            SaleEntity(title = "USB-C Fast Charging Cable 2m", price = 12.50, cost = 3.20, timestamp = now - (oneHour * 2)),
            SaleEntity(title = "Mechanical Keyboard Blue Switch", price = 79.99, cost = 42.00, timestamp = now - (oneHour * 4)),
            SaleEntity(title = "Tempered Glass Screen Protector", price = 9.99, cost = 1.50, timestamp = now - (oneHour * 5)),
            SaleEntity(title = "Bluetooth 5.3 Earbuds", price = 45.00, cost = 22.00, timestamp = now - (oneHour * 6)),
            SaleEntity(title = "Laptop Aluminum Stand", price = 28.00, cost = 12.00, timestamp = now - (oneHour * 8)),
            SaleEntity(title = "Magnetic Phone Car Mount", price = 15.00, cost = 4.50, timestamp = now - (oneDay * 1 + oneHour * 2)),
            SaleEntity(title = "65W GaN Wall Charger", price = 32.00, cost = 14.80, timestamp = now - (oneDay * 1 + oneHour * 5)),
            SaleEntity(title = "Desk Mousepad XXL", price = 18.00, cost = 6.00, timestamp = now - (oneDay * 2 + oneHour * 3)),
            SaleEntity(title = "Webcam 1080p HD with Mic", price = 52.00, cost = 27.50, timestamp = now - (oneDay * 2 + oneHour * 7)),
            SaleEntity(title = "Cable Management Ties (50pk)", price = 7.50, cost = 1.80, timestamp = now - (oneDay * 3 + oneHour * 4)),
            SaleEntity(title = "Portable Power Bank 20000mAh", price = 42.00, cost = 23.00, timestamp = now - (oneDay * 4 + oneHour * 1)),
            SaleEntity(title = "Microfiber Cleaning Kit", price = 8.00, cost = 2.10, timestamp = now - (oneDay * 5 + oneHour * 6))
        )
        saleDao.insertSales(samples)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }
}
