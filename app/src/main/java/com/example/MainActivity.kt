package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.QuickSaleScreen
import com.example.ui.screens.SalesLogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab(val label: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    QUICK_SALE("Quick Sale", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    SALES_LOG("Sales Log", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
                val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
                val storeName by viewModel.storeName.collectAsStateWithLifecycle()
                val dailyTargetProfit by viewModel.dailyTargetProfit.collectAsStateWithLifecycle()
                val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
                val allSales by viewModel.allSales.collectAsStateWithLifecycle()
                val recentTitles by viewModel.recentTitles.collectAsStateWithLifecycle()
                val filteredSales by viewModel.filteredSales.collectAsStateWithLifecycle()
                val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
                val marginFilter by viewModel.marginFilter.collectAsStateWithLifecycle()
                val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val selectedSpecificDateMillis by viewModel.selectedSpecificDateMillis.collectAsStateWithLifecycle()
                val analyticsReport by viewModel.currentAnalyticsReport.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(AppTab.QUICK_SALE) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Listen to ViewModel snackbar events
                LaunchedEffect(Unit) {
                    viewModel.snackMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                // If locked, show full-screen PinLockScreen
                if (!isUnlocked) {
                    // Try triggering biometric prompt if enabled
                    LaunchedEffect(Unit) {
                        viewModel.triggerBiometricAuth(this@MainActivity)
                    }

                    PinLockScreen(
                        title = "Hisab Security",
                        subtitle = "Enter 4-digit PIN to access store sales",
                        showBiometricButton = viewModel.securityManager.isBiometricEnabled(),
                        onPinComplete = { pin ->
                            viewModel.unlockWithPin(pin)
                        },
                        onBiometricClick = {
                            viewModel.triggerBiometricAuth(this@MainActivity)
                        },
                        onForgotPin = {
                            viewModel.resetPinSecurity()
                        }
                    )
                } else {
                    // Back handler: return to Quick Sale tab if on another tab
                    BackHandler(enabled = currentTab != AppTab.QUICK_SALE) {
                        currentTab = AppTab.QUICK_SALE
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets.statusBars,
                        topBar = {
                            HisabTopBar(
                                currentTab = currentTab,
                                storeName = storeName,
                                isPinEnabled = viewModel.securityManager.isPinEnabled(),
                                onLockClick = { viewModel.lockApp() }
                            )
                        },
                        bottomBar = {
                            HisabBottomNavigationBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        },
                        containerColor = BackgroundLight,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    if (targetState.ordinal > initialState.ordinal) {
                                        (slideInHorizontally { width -> width / 3 } + fadeIn())
                                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
                                    } else {
                                        (slideInHorizontally { width -> -width / 3 } + fadeIn())
                                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut())
                                    }
                                },
                                label = "tab_transition"
                            ) { tab ->
                                when (tab) {
                                    AppTab.QUICK_SALE -> {
                                        QuickSaleScreen(
                                            todaySummary = todaySummary,
                                            recentSales = allSales,
                                            recentTitles = recentTitles,
                                            currencySymbol = currencySymbol,
                                            storeName = storeName,
                                            dailyTargetProfit = dailyTargetProfit,
                                            onRecordSale = { title, price, cost, quantity ->
                                                viewModel.recordSale(title, price, cost, quantity)
                                            },
                                            onDuplicateSale = { sale ->
                                                viewModel.duplicateSale(sale)
                                            },
                                            onDeleteSale = { sale ->
                                                viewModel.deleteSale(sale)
                                            },
                                            onNavigateToLog = {
                                                currentTab = AppTab.SALES_LOG
                                            }
                                        )
                                    }

                                    AppTab.SALES_LOG -> {
                                        SalesLogScreen(
                                            sales = filteredSales,
                                            timeFilter = timeFilter,
                                            marginFilter = marginFilter,
                                            sortOrder = sortOrder,
                                            searchQuery = searchQuery,
                                            selectedSpecificDateMillis = selectedSpecificDateMillis,
                                            currencySymbol = currencySymbol,
                                            onTimeFilterChange = { viewModel.setTimeFilter(it) },
                                            onMarginFilterChange = { viewModel.setMarginFilter(it) },
                                            onSortOrderChange = { viewModel.setSortOrder(it) },
                                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                            onSelectSpecificDate = { viewModel.setSelectedSpecificDate(it) },
                                            onDuplicateSale = { viewModel.duplicateSale(it) },
                                            onUpdateSale = { viewModel.updateSale(it) },
                                            onDeleteSale = { viewModel.deleteSale(it) }
                                        )
                                    }

                                    AppTab.ANALYTICS -> {
                                        AnalyticsScreen(
                                            report = analyticsReport,
                                            timeFilter = timeFilter,
                                            currencySymbol = currencySymbol,
                                            onTimeFilterChange = { viewModel.setTimeFilter(it) }
                                        )
                                    }

                                    AppTab.SETTINGS -> {
                                        SettingsScreen(
                                            isPinEnabled = viewModel.securityManager.isPinEnabled(),
                                            isBiometricEnabled = viewModel.securityManager.isBiometricEnabled(),
                                            canUseBiometrics = viewModel.securityManager.canUseBiometrics(),
                                            currentCurrency = currencySymbol,
                                            storeName = storeName,
                                            dailyTargetProfit = dailyTargetProfit,
                                            onSetupPin = { viewModel.setupNewPin(it) },
                                            onDisablePin = { viewModel.removePinSecurity() },
                                            onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                                            onSelectCurrency = { viewModel.setCurrency(it) },
                                            onUpdateStoreName = { viewModel.setStoreName(it) },
                                            onUpdateDailyTarget = { viewModel.setDailyTarget(it) },
                                            onTestBiometrics = { viewModel.triggerBiometricAuth(this@MainActivity) },
                                            onExportCsv = { viewModel.exportDataCsv() },
                                            onExportJson = { viewModel.exportDataJson() },
                                            onImportCsv = { raw -> viewModel.importCsv(raw) {} },
                                            onImportJson = { raw -> viewModel.importJson(raw) {} },
                                            onSeedSampleData = { viewModel.seedSampleData() },
                                            onClearAllData = { viewModel.clearAllSales() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HisabTopBar(
    currentTab: AppTab,
    storeName: String = "My Store",
    isPinEnabled: Boolean,
    onLockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .border(0.dp, BorderSubtle)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Emblem
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = ProfitEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Hisab",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ProfitEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFLINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ProfitEmerald
                            )
                        }
                    }
                    Text(
                        text = "$storeName • ${currentTab.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            if (isPinEnabled) {
                IconButton(
                    onClick = onLockClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                        .testTag("lock_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock App",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HisabBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 2.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, BorderSubtle)
            .testTag("bottom_nav_bar")
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandDark,
                    selectedTextColor = BrandDark,
                    indicatorColor = SurfaceSubtle,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
