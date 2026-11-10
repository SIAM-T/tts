package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.AnalyticsEngine
import com.example.analytics.SalesAnalyticsReport
import com.example.data.AppDatabase
import com.example.data.SaleRepository
import com.example.model.SaleEntity
import com.example.security.SecurityManager
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
import java.util.Calendar

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
    val count: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = SaleRepository(db.saleDao())
    val securityManager = SecurityManager(application)

    private val _isUnlocked = MutableStateFlow(!securityManager.isPinEnabled())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

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

    // All sales stream
    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTitles: StateFlow<List<String>> = repository.recentTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's summary computation
    val todaySummary: StateFlow<TodaySummary> = allSales.combine(_timeFilter) { sales, _ ->
        val todayItems = sales.filter { AnalyticsEngine.isToday(it.timestamp) }
        val rev = todayItems.sumOf { it.price * it.quantity }
        val cost = todayItems.sumOf { it.cost * it.quantity }
        val prof = rev - cost
        val margin = if (rev > 0) (prof / rev) * 100.0 else 0.0
        val count = todayItems.sumOf { it.quantity }
        TodaySummary(
            revenue = rev,
            cost = cost,
            profit = prof,
            marginPercent = margin,
            count = count
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

    // Filtered and sorted sales for Sales Log tab
    val filteredSales: StateFlow<List<SaleEntity>> = combine(allSales, filterParams) { sales, filter ->
        var list = sales

        // 1. Specific date or time preset
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

        // 2. Margin Tier
        list = when (filter.margin) {
            MarginFilterTier.ALL -> list
            MarginFilterTier.HIGH -> list.filter { it.marginPercent >= 35.0 }
            MarginFilterTier.MODERATE -> list.filter { it.marginPercent in 15.0..34.999 }
            MarginFilterTier.LOW -> list.filter { it.marginPercent in 0.0..14.999 }
            MarginFilterTier.LOSS -> list.filter { it.marginPercent < 0.0 }
        }

        // 3. Search query
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.note.lowercase().contains(q) }
        }

        // 4. Sort Order
        list = when (filter.sort) {
            SortOrder.NEWEST -> list.sortedByDescending { it.timestamp }
            SortOrder.HIGHEST_PROFIT -> list.sortedByDescending { it.profit }
            SortOrder.HIGHEST_MARGIN -> list.sortedByDescending { it.marginPercent }
            SortOrder.HIGHEST_PRICE -> list.sortedByDescending { it.price * it.quantity }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics report based on active time filter
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
            val prefix = if (sale.profit >= 0) "+" else ""
            _snackMessage.emit("Sale recorded! Profit: $prefix${_currencySymbol.value}${String.format("%.2f", sale.profit)}")
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
            _snackMessage.emit("Sale updated successfully")
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
            _snackMessage.emit("All sales cleared")
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _snackMessage.emit("13 realistic store sales loaded!")
        }
    }

    fun setCurrency(symbol: String) {
        securityManager.setCurrency(symbol)
        _currencySymbol.value = symbol
    }

    fun setStoreName(name: String) {
        securityManager.setStoreName(name)
        _storeName.value = securityManager.getStoreName()
        viewModelScope.launch {
            _snackMessage.emit("Store name updated!")
        }
    }

    fun setDailyTarget(target: Double) {
        securityManager.setDailyTargetProfit(target)
        _dailyTargetProfit.value = target
        viewModelScope.launch {
            _snackMessage.emit("Daily profit goal set to ${_currencySymbol.value}${String.format("%.2f", target)}")
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
        if (securityManager.isPinEnabled()) {
            _isUnlocked.value = false
        }
    }

    fun resetPinSecurity() {
        securityManager.removePin()
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("PIN reset. Security turned off.")
        }
    }

    fun setupNewPin(pin: String) {
        securityManager.setPin(pin)
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("PIN lock activated successfully!")
        }
    }

    fun removePinSecurity() {
        securityManager.removePin()
        _isUnlocked.value = true
        viewModelScope.launch {
            _snackMessage.emit("PIN lock disabled")
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
    }

    fun triggerBiometricAuth(activity: Activity) {
        if (!securityManager.isBiometricEnabled()) return
        securityManager.triggerBiometricPrompt(
            activity = activity,
            onSuccess = {
                _isUnlocked.value = true
                viewModelScope.launch {
                    _snackMessage.emit("Unlocked via Biometrics!")
                }
            },
            onError = { err ->
                // Biometrics error/cancellation reported gracefully
            }
        )
    }

    suspend fun exportDataCsv(): String {
        return repository.exportToCsv(allSales.value)
    }

    suspend fun exportDataJson(): String {
        return repository.exportToJson(allSales.value)
    }

    fun importCsv(content: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromCsv(content)
            val count = result.getOrDefault(0)
            _snackMessage.emit("Imported $count sales successfully")
            onComplete(count)
        }
    }

    fun importJson(content: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(content)
            val count = result.getOrDefault(0)
            _snackMessage.emit("Imported $count sales successfully")
            onComplete(count)
        }
    }
}
