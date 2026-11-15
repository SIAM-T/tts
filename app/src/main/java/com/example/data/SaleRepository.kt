package com.example.data

import com.example.model.SaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
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

    /**
     * Streams CSV directly to any OutputStream (handles huge datasets cleanly with minimal RAM).
     */
    suspend fun exportToCsvStream(sales: List<SaleEntity>, outputStream: OutputStream): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
                    writer.write("ID,Date,Title,Price,Cost,Profit,MarginPercent,Quantity,Note\n")
                    sales.forEach { s ->
                        val escapedTitle = "\"" + s.title.replace("\"", "\"\"") + "\""
                        val escapedNote = "\"" + s.note.replace("\"", "\"\"") + "\""
                        val dateStr = sdf.format(Date(s.timestamp))
                        val line = buildString {
                            append(s.id).append(',')
                            append(dateStr).append(',')
                            append(escapedTitle).append(',')
                            append(s.price).append(',')
                            append(s.cost).append(',')
                            append(s.profit).append(',')
                            append(String.format(Locale.US, "%.2f", s.marginPercent)).append(',')
                            append(s.quantity).append(',')
                            append(escapedNote).append('\n')
                        }
                        writer.write(line)
                    }
                    writer.flush()
                }
                sales.size
            }
        }

    /**
     * Streams JSON directly to any OutputStream (handles large backups cleanly).
     */
    suspend fun exportToJsonStream(sales: List<SaleEntity>, outputStream: OutputStream): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
                    writer.write("[\n")
                    sales.forEachIndexed { idx, s ->
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
                        writer.write("  ")
                        writer.write(obj.toString())
                        if (idx < sales.lastIndex) {
                            writer.write(",\n")
                        } else {
                            writer.write("\n")
                        }
                    }
                    writer.write("]\n")
                    writer.flush()
                }
                sales.size
            }
        }

    /**
     * Imports either CSV or JSON from an InputStream with buffered batch inserts.
     */
    suspend fun importFromStream(inputStream: InputStream, fileNameHint: String = ""): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use {
                    it.readText()
                }
                val trimmed = content.trimStart()
                if (trimmed.isEmpty()) return@runCatching 0

                val isJson = fileNameHint.endsWith(".json", ignoreCase = true) ||
                    trimmed.startsWith("[") || trimmed.startsWith("{")

                if (isJson) {
                    importFromJson(trimmed).getOrThrow()
                } else {
                    importFromCsv(trimmed).getOrThrow()
                }
            }
        }

    suspend fun importFromCsv(csvContent: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return@runCatching 0

            val firstTokens = parseCsvLine(lines[0]).map { it.lowercase(Locale.US) }
            val hasHeader = firstTokens.any {
                it.contains("price") || it.contains("title") || it.contains("cost")
            }

            // Detect column indices dynamically from header if present, else infer format
            var idxDate = 1
            var idxTitle = 2
            var idxPrice = 3
            var idxCost = 4
            var idxQty = 7
            var idxNote = 8

            if (hasHeader) {
                idxDate = firstTokens.indexOfFirst { it.contains("date") || it.contains("time") }
                idxTitle = firstTokens.indexOfFirst { it.contains("title") || it.contains("item") || it.contains("name") || it.contains("product") }
                idxPrice = firstTokens.indexOfFirst { it.contains("price") || it.contains("sell") || it.contains("revenue") }
                idxCost = firstTokens.indexOfFirst { it.contains("cost") }
                idxQty = firstTokens.indexOfFirst { it.contains("qty") || it.contains("quantity") }
                idxNote = firstTokens.indexOfFirst { it.contains("note") }
            } else if (firstTokens.size in 3..4) {
                // Simple 3-column or 4-column CSV: Title, Price, Cost [, Quantity]
                idxDate = -1
                idxTitle = 0
                idxPrice = 1
                idxCost = 2
                idxQty = if (firstTokens.size >= 4) 3 else -1
                idxNote = -1
            }

            val startIdx = if (hasHeader) 1 else 0
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val batch = mutableListOf<SaleEntity>()
            var totalInserted = 0

            for (i in startIdx until lines.size) {
                val tokens = parseCsvLine(lines[i])
                if (tokens.size < 3) continue

                val title = tokens.getOrNull(idxTitle)?.ifBlank { "Imported Item" } ?: "Imported Item"
                val price = tokens.getOrNull(idxPrice)?.toDoubleOrNull() ?: continue
                val cost = tokens.getOrNull(idxCost)?.toDoubleOrNull() ?: 0.0
                val qty = (if (idxQty >= 0) tokens.getOrNull(idxQty)?.toIntOrNull() else null)?.coerceAtLeast(1) ?: 1
                val note = if (idxNote >= 0) tokens.getOrNull(idxNote) ?: "" else ""
                val time = if (idxDate >= 0) {
                    tokens.getOrNull(idxDate)?.let { raw ->
                        raw.toLongOrNull() ?: try { sdf.parse(raw)?.time } catch (_: Exception) { null }
                    }
                } else null

                val margin = if (price > 0) ((price - cost) / price) * 100.0 else 0.0
                batch.add(
                    SaleEntity(
                        id = 0,
                        title = title,
                        price = price,
                        cost = cost,
                        profit = (price - cost) * qty,
                        marginPercent = margin,
                        timestamp = time ?: System.currentTimeMillis(),
                        quantity = qty,
                        note = note
                    )
                )

                if (batch.size >= 500) {
                    saleDao.insertSales(batch)
                    totalInserted += batch.size
                    batch.clear()
                }
            }

            if (batch.isNotEmpty()) {
                saleDao.insertSales(batch)
                totalInserted += batch.size
            }
            totalInserted
        }
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = jsonString.trim()
            val array = if (trimmed.startsWith("{")) {
                val wrapper = JSONObject(trimmed)
                wrapper.optJSONArray("sales") ?: JSONArray().apply { put(wrapper) }
            } else {
                JSONArray(trimmed)
            }

            val batch = mutableListOf<SaleEntity>()
            var totalInserted = 0

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Item").ifBlank { "Item" }
                val price = obj.optDouble("price", 0.0)
                val cost = obj.optDouble("cost", 0.0)
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val quantity = obj.optInt("quantity", 1).coerceAtLeast(1)
                val note = obj.optString("note", "")

                batch.add(
                    SaleEntity(
                        id = 0,
                        title = title,
                        price = price,
                        cost = cost,
                        profit = (price - cost) * quantity,
                        marginPercent = if (price > 0) ((price - cost) / price) * 100.0 else 0.0,
                        timestamp = timestamp,
                        quantity = quantity,
                        note = note
                    )
                )

                if (batch.size >= 500) {
                    saleDao.insertSales(batch)
                    totalInserted += batch.size
                    batch.clear()
                }
            }
            if (batch.isNotEmpty()) {
                saleDao.insertSales(batch)
                totalInserted += batch.size
            }
            totalInserted
        }
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        val samples = listOf(
            SaleEntity(title = "Wireless Ergonomic Mouse", price = 34.99, cost = 16.50, timestamp = now - (oneHour * 1)),
            SaleEntity(title = "USB-C Fast Charging Cable 2m", price = 12.50, cost = 3.20, quantity = 2, profit = (12.50 - 3.20) * 2, marginPercent = ((12.50 - 3.20) / 12.50) * 100.0, timestamp = now - (oneHour * 2)),
            SaleEntity(title = "Mechanical Keyboard Blue Switch", price = 79.99, cost = 42.00, timestamp = now - (oneHour * 4)),
            SaleEntity(title = "Tempered Glass Screen Protector", price = 9.99, cost = 1.50, quantity = 3, profit = (9.99 - 1.50) * 3, marginPercent = ((9.99 - 1.50) / 9.99) * 100.0, timestamp = now - (oneHour * 5)),
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
        val current = StringBuilder()
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
