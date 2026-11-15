package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.AnalyticsEngine
import com.example.analytics.SalesAnalyticsReport
import com.example.data.AppDatabase
import com.example.data.SaleRepository
import com.example.model.SaleEntity
import com.example.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days"),
    ALL_TIME("All Time")
}

enum class MarginFilterTier(val label: String) {
    ALL("All Margins"),
    HIGH("High (>35%)"),
    MODERATE("Healthy (15-35%)"),
    LOW("Slim (0-15%)"),
    LOSS("Loss (<0%)")
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    HIGHEST_PROFIT("Highest Profit"),
    HIGHEST_MARGIN("Highest Margin %"),
    HIGHEST_PRICE("Highest Price")
}

data class TodaySummary(
    val revenue: Double = 0.0,
    val cost: Double = 0.0,
    val profit: Double = 0.0,
    val marginPercent: Double = 0.0,
    val count: Int = 0,
    val transactionCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = SaleRepository(db.saleDao())
    val securityManager = SecurityManager(application)

    private val _isUnlocked = MutableStateFlow(!securityManager.isPinEnabled())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(securityManager.isPinEnabled())
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isAutoLockEnabled = MutableStateFlow(securityManager.isAutoLockEnabled())
    val isAutoLockEnabled: StateFlow<Boolean> = _isAutoLockEnabled.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(securityManager.isHapticsEnabled())
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    private val _currencySymbol = MutableStateFlow(securityManager.getCurrency())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _storeName = MutableStateFlow(securityManager.getStoreName())
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _dailyTargetProfit = MutableStateFlow(securityManager.getDailyTargetProfit())
    val dailyTargetProfit: StateFlow<Double> = _dailyTargetProfit.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeFilter.TODAY)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    private val _marginFilter = MutableStateFlow(MarginFilterTier.ALL)
    val marginFilter: StateFlow<MarginFilterTier> = _marginFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpecificDateMillis = MutableStateFlow<Long?>(null)
    val selectedSpecificDateMillis: StateFlow<Long?> = _selectedSpecificDateMillis.asStateFlow()

    private val _snackMessage = MutableSharedFlow<String>()
    val snackMessage: SharedFlow<String> = _snackMessage.asSharedFlow()

    private var suppressNextAutoLock = false

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTitles: StateFlow<List<String>> = repository.recentTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySummary: StateFlow<TodaySummary> = allSales.combine(_timeFilter) { sales, _ ->
        val todayItems = sales.filter { AnalyticsEngine.isToday(it.timestamp) }
        val rev = todayItems.sumOf { it.price * it.quantity }
        val cost = todayItems.sumOf { it.cost * it.quantity }
        val prof = rev - cost
        val margin = if (rev > 0) (prof / rev) * 100.0 else 0.0
        val unitCount = todayItems.sumOf { it.quantity }
        TodaySummary(
            revenue = rev,
            cost = cost,
            profit = prof,
            marginPercent = margin,
            count = unitCount,
            transactionCount = todayItems.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodaySummary())

    private data class FilterState(
        val time: TimeFilter,
        val margin: MarginFilterTier,
        val query: String,
        val specificDate: Long?,
        val sort: SortOrder
    )

    private val filterParams = combine(
        _timeFilter,
        _marginFilter,
        _searchQuery,
        _selectedSpecificDateMillis,
        _sortOrder
    ) { time, margin, query, specificDate, sort ->
        FilterState(time, margin, query, specificDate, sort)
    }

    val filteredSales: StateFlow<List<SaleEntity>> = combine(allSales, filterParams) { sales, filter ->
        var list = sales

        if (filter.specificDate != null) {
            val targetCal = Calendar.getInstance().apply { timeInMillis = filter.specificDate }
            list = list.filter { s ->
                val sCal = Calendar.getInstance().apply { timeInMillis = s.timestamp }
                sCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                        sCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
            }
        } else {
            list = when (filter.time) {
                TimeFilter.TODAY -> list.filter { AnalyticsEngine.isToday(it.timestamp) }
                TimeFilter.YESTERDAY -> list.filter { AnalyticsEngine.isYesterday(it.timestamp) }
                TimeFilter.LAST_7_DAYS -> list.filter { AnalyticsEngine.isInLastDays(it.timestamp, 7) }
                TimeFilter.LAST_30_DAYS -> list.filter { AnalyticsEngine.isInLastDays(it.timestamp, 30) }
                TimeFilter.ALL_TIME -> list
            }
        }

        list = when (filter.margin) {
            MarginFilterTier.ALL -> list
            MarginFilterTier.HIGH -> list.filter { it.marginPercent >= 35.0 }
            MarginFilterTier.MODERATE -> list.filter { it.marginPercent in 15.0..34.999 }
            MarginFilterTier.LOW -> list.filter { it.marginPercent in 0.0..14.999 }
            MarginFilterTier.LOSS -> list.filter { it.marginPercent < 0.0 }
        }

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.note.lowercase().contains(q) }
        }

        list = when (filter.sort) {
            SortOrder.NEWEST -> list.sortedByDescending { it.timestamp }
            SortOrder.HIGHEST_PROFIT -> list.sortedByDescending { it.profit }
            SortOrder.HIGHEST_MARGIN -> list.sortedByDescending { it.marginPercent }
            SortOrder.HIGHEST_PRICE -> list.sortedByDescending { it.price * it.quantity }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAnalyticsReport: StateFlow<SalesAnalyticsReport> = combine(
        allSales,
        _timeFilter,
        _selectedSpecificDateMillis
    ) { sales, time, specificDate ->
        val list = if (specificDate != null) {
            val targetCal = Calendar.getInstance().apply { timeInMillis = specificDate }
            sales.filter { s ->
                val sCal = Calendar.getInstance().apply { timeInMillis = s.timestamp }
                sCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                        sCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
            }
        } else {
            when (time) {
                TimeFilter.TODAY -> sales.filter { AnalyticsEngine.isToday(it.timestamp) }
                TimeFilter.YESTERDAY -> sales.filter { AnalyticsEngine.isYesterday(it.timestamp) }
                TimeFilter.LAST_7_DAYS -> sales.filter { AnalyticsEngine.isInLastDays(it.timestamp, 7) }
                TimeFilter.LAST_30_DAYS -> sales.filter { AnalyticsEngine.isInLastDays(it.timestamp, 30) }
                TimeFilter.ALL_TIME -> sales
            }
        }
        AnalyticsEngine.generateReport(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsEngine.generateReport(emptyList()))

    fun setTimeFilter(filter: TimeFilter) {
        _selectedSpecificDateMillis.value = null
        _timeFilter.value = filter
    }

    fun setMarginFilter(filter: MarginFilterTier) {
        _marginFilter.value = filter
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedSpecificDate(millis: Long?) {
        _selectedSpecificDateMillis.value = millis
    }

    fun recordSale(title: String, price: Double, cost: Double, quantity: Int = 1, note: String = "", customTimestamp: Long? = null) {
        viewModelScope.launch {
            val finalPrice = if (price < 0) 0.0 else price
            val finalCost = if (cost < 0) 0.0 else cost
            val time = customTimestamp ?: System.currentTimeMillis()
            val cleanTitle = if (title.isBlank()) "Quick Sale" else title.trim()

            val sale = SaleEntity(
                title = cleanTitle,
                price = finalPrice,
                cost = finalCost,
                profit = (finalPrice - finalCost) * quantity,
                marginPercent = if (finalPrice > 0) ((finalPrice - finalCost) / finalPrice) * 100.0 else 0.0,
                timestamp = time,
                quantity = quantity.coerceAtLeast(1),
                note = note.trim()
            )
            repository.insertSale(sale)
        }
    }

    fun duplicateSale(sale: SaleEntity) {
        viewModelScope.launch {
            val newSale = sale.copy(
                id = 0,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSale(newSale)
            _snackMessage.emit("Repeated '${newSale.title}'")
        }
    }

    fun updateSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.updateSale(sale)
            _snackMessage.emit("Sale updated")
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
            _snackMessage.emit("Sale removed")
        }
    }

    fun clearAllSales() {
        viewModelScope.launch {
            repository.clearAll()
            _snackMessage.emit("All sales data cleared")
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _snackMessage.emit("13 realistic store sales loaded!")
        }
    }

    // Reactive Settings Mutations
    fun setCurrency(symbol: String) {
        securityManager.setCurrency(symbol)
        _currencySymbol.value = securityManager.getCurrency()
        viewModelScope.launch {
            _snackMessage.emit("Currency set to ${_currencySymbol.value}")
        }
    }

    fun setStoreName(name: String) {
        securityManager.setStoreName(name)
        _storeName.value = securityManager.getStoreName()
        viewModelScope.launch {
            _snackMessage.emit("Store renamed to '${_storeName.value}'")
        }
    }

    fun setDailyTarget(target: Double) {
        securityManager.setDailyTargetProfit(target)
        _dailyTargetProfit.value = securityManager.getDailyTargetProfit()
        viewModelScope.launch {
            _snackMessage.emit("Daily goal updated to ${_currencySymbol.value}${_dailyTargetProfit.value.toInt()}")
        }
    }

    fun setAutoLockEnabled(enabled: Boolean) {
        securityManager.setAutoLockEnabled(enabled)
        _isAutoLockEnabled.value = securityManager.isAutoLockEnabled()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        securityManager.setHapticsEnabled(enabled)
        _isHapticsEnabled.value = securityManager.isHapticsEnabled()
    }

    fun suppressAutoLockOnce() {
        suppressNextAutoLock = true
    }

    fun onAppBackgrounded() {
        if (suppressNextAutoLock) {
            suppressNextAutoLock = false
            return
        }
        if (_isPinEnabled.value && _isAutoLockEnabled.value) {
            _isUnlocked.value = false
        }
    }

    // Security Unlock / Lock
    fun unlockWithPin(pin: String): Boolean {
        val success = securityManager.verifyPin(pin)
        if (success) {
            _isUnlocked.value = true
        }
        return success
    }

    fun lockApp() {
        if (_isPinEnabled.value) {
            _isUnlocked.value = false
        }
    }

    fun resetPinSecurity() {
        securityManager.removePin()
        _isPinEnabled.value = false
        _isBiometricEnabled.value = false
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("PIN reset. Security turned off.")
        }
    }

    fun setupNewPin(pin: String) {
        securityManager.setPin(pin)
        _isPinEnabled.value = true
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("4-digit PIN lock activated!")
        }
    }

    fun removePinSecurity() {
        securityManager.removePin()
        _isPinEnabled.value = false
        _isBiometricEnabled.value = false
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("PIN lock disabled")
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        if (enabled && !securityManager.canUseBiometrics()) {
            viewModelScope.launch {
                _snackMessage.emit(securityManager.getBiometricStatusMessage())
            }
            return
        }
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = securityManager.isBiometricEnabled()
        viewModelScope.launch {
            _snackMessage.emit(if (_isBiometricEnabled.value) "Biometric unlock enabled" else "Biometric unlock disabled")
        }
    }

    fun triggerBiometricAuth(activity: Activity, isManualTest: Boolean = false) {
        if (!isManualTest && !_isBiometricEnabled.value) return
        securityManager.triggerBiometricPrompt(
            activity = activity,
            onSuccess = {
                _isUnlocked.value = true
                viewModelScope.launch {
                    _snackMessage.emit(if (isManualTest) "Biometric verification passed!" else "Unlocked with Biometrics")
                }
            },
            onError = { err ->
                if (isManualTest || err.contains("unavailable", ignoreCase = true) || err.contains("enrolled", ignoreCase = true)) {
                    viewModelScope.launch {
                        _snackMessage.emit(err)
                    }
                }
            }
        )
    }

    // Real File-Based Streaming Exports & Imports
    fun saveExportToUri(uri: Uri, isCsv: Boolean) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val sales = allSales.value
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openOutputStream(uri)?.use { os ->
                        if (isCsv) {
                            repository.exportToCsvStream(sales, os).getOrThrow()
                        } else {
                            repository.exportToJsonStream(sales, os).getOrThrow()
                        }
                    } ?: throw IllegalStateException("Could not open file destination")
                }
            }
            result.onSuccess { count ->
                val ext = if (isCsv) "CSV" else "JSON"
                _snackMessage.emit("Saved $count sales to $ext file!")
            }.onFailure { err ->
                _snackMessage.emit("Export failed: ${err.localizedMessage ?: "Write error"}")
            }
        }
    }

    fun createShareFileIntent(isCsv: Boolean, onIntentReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val sales = allSales.value
            val dateStamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
            val ext = if (isCsv) "csv" else "json"
            val mimeType = if (isCsv) "text/csv" else "application/json"
            val fileName = "Hisab_Sales_$dateStamp.$ext"

            val fileResult = withContext(Dispatchers.IO) {
                runCatching {
                    val exportDir = File(app.cacheDir, "exports").apply { mkdirs() }
                    // Clean old cached exports
                    exportDir.listFiles()?.forEach { it.delete() }
                    val file = File(exportDir, fileName)
                    FileOutputStream(file).use { fos ->
                        if (isCsv) {
                            repository.exportToCsvStream(sales, fos).getOrThrow()
                        } else {
                            repository.exportToJsonStream(sales, fos).getOrThrow()
                        }
                    }
                    FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                }
            }

            fileResult.onSuccess { uri ->
                suppressAutoLockOnce()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Hisab Store Backup ($fileName)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                onIntentReady(Intent.createChooser(shareIntent, "Share $ext Backup File"))
            }.onFailure { err ->
                _snackMessage.emit("Could not create export file: ${err.localizedMessage}")
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val fileName = uri.lastPathSegment ?: ""
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        repository.importFromStream(input, fileName).getOrThrow()
                    } ?: throw IllegalStateException("Could not open selected file")
                }
            }
            result.onSuccess { count ->
                if (count > 0) {
                    _snackMessage.emit("Imported $count sales from file!")
                } else {
                    _snackMessage.emit("No valid sales records found in file")
                }
            }.onFailure { err ->
                _snackMessage.emit("Import failed: ${err.localizedMessage ?: "Invalid file format"}")
            }
        }
    }

    fun importFromText(rawContent: String, isCsv: Boolean) {
        viewModelScope.launch {
            val result = if (isCsv) {
                repository.importFromCsv(rawContent)
            } else {
                repository.importFromJson(rawContent)
            }
            result.onSuccess { count ->
                _snackMessage.emit("Imported $count sales successfully")
            }.onFailure { err ->
                _snackMessage.emit("Import failed: ${err.localizedMessage ?: "Invalid syntax"}")
            }
        }
    }
}
