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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.PinLockScreen
import com.example.ui.components.ambientFinancialBackdrop
import com.example.ui.components.performHapticTap
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.QuickSaleScreen
import com.example.ui.screens.SalesLogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBg
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

enum class AppTab(val label: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    QUICK_SALE("Quick Sale", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    SALES_LOG("Sales Log", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current

                // Reactive StateFlows
                val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
                val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                val isAutoLockEnabled by viewModel.isAutoLockEnabled.collectAsStateWithLifecycle()
                val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
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

                // Background auto-lock observer (respects suppressAutoLockOnce for system file pickers)
                LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                    viewModel.onAppBackgrounded()
                }

                LaunchedEffect(Unit) {
                    viewModel.snackMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                if (!isUnlocked) {
                    LaunchedEffect(isUnlocked, isBiometricEnabled) {
                        if (!isUnlocked && isBiometricEnabled) {
                            viewModel.triggerBiometricAuth(this@MainActivity, isManualTest = false)
                        }
                    }

                    PinLockScreen(
                        title = storeName,
                        subtitle = "Enter 4-digit PIN to access store ledger",
                        showBiometricButton = isBiometricEnabled && viewModel.securityManager.canUseBiometrics(),
                        hapticsEnabled = isHapticsEnabled,
                        onPinComplete = { pin -> viewModel.unlockWithPin(pin) },
                        onBiometricClick = {
                            viewModel.triggerBiometricAuth(this@MainActivity, isManualTest = false)
                        },
                        onForgotPin = { viewModel.resetPinSecurity() }
                    )
                } else {
                    BackHandler(enabled = currentTab != AppTab.QUICK_SALE) {
                        currentTab = AppTab.QUICK_SALE
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .ambientFinancialBackdrop()
                    ) {
                        val useNavRail = maxWidth >= 600.dp

                        Row(modifier = Modifier.fillMaxSize()) {
                            if (useNavRail) {
                                HisabNavigationRail(
                                    currentTab = currentTab,
                                    onTabSelected = {
                                        performHapticTap(context, isHapticsEnabled)
                                        currentTab = it
                                    }
                                )
                                VerticalDivider(color = BorderHairline)
                            }

                            Scaffold(
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                                topBar = {
                                    HisabTopBar(
                                        currentTab = currentTab,
                                        storeName = storeName,
                                        isPinEnabled = isPinEnabled,
                                        onLockClick = {
                                            performHapticTap(context, isHapticsEnabled)
                                            viewModel.lockApp()
                                        }
                                    )
                                },
                                bottomBar = {
                                    if (!useNavRail) {
                                        HisabBottomNavigationBar(
                                            currentTab = currentTab,
                                            onTabSelected = {
                                                performHapticTap(context, isHapticsEnabled)
                                                currentTab = it
                                            }
                                        )
                                    }
                                },
                                containerColor = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .then(
                                            if (useNavRail) {
                                                Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    AnimatedContent(
                                        targetState = currentTab,
                                        transitionSpec = {
                                            if (targetState.ordinal > initialState.ordinal) {
                                                (slideInHorizontally { width -> width / 4 } + fadeIn())
                                                    .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut())
                                            } else {
                                                (slideInHorizontally { width -> -width / 4 } + fadeIn())
                                                    .togetherWith(slideOutHorizontally { width -> width / 4 } + fadeOut())
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
                                                    hapticsEnabled = isHapticsEnabled,
                                                    onRecordSale = { title, price, cost, quantity ->
                                                        viewModel.recordSale(title, price, cost, quantity)
                                                    },
                                                    onDuplicateSale = { viewModel.duplicateSale(it) },
                                                    onDeleteSale = { viewModel.deleteSale(it) },
                                                    onNavigateToLog = { currentTab = AppTab.SALES_LOG }
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
                                                    hapticsEnabled = isHapticsEnabled,
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
                                                    selectedSpecificDateMillis = selectedSpecificDateMillis,
                                                    currencySymbol = currencySymbol,
                                                    dailyTargetProfit = dailyTargetProfit,
                                                    hapticsEnabled = isHapticsEnabled,
                                                    onTimeFilterChange = { viewModel.setTimeFilter(it) },
                                                    onSelectSpecificDate = { viewModel.setSelectedSpecificDate(it) }
                                                )
                                            }

                                            AppTab.SETTINGS -> {
                                                SettingsScreen(
                                                    isPinEnabled = isPinEnabled,
                                                    isBiometricEnabled = isBiometricEnabled,
                                                    isAutoLockEnabled = isAutoLockEnabled,
                                                    isHapticsEnabled = isHapticsEnabled,
                                                    canUseBiometrics = viewModel.securityManager.canUseBiometrics(),
                                                    biometricStatusText = viewModel.securityManager.getBiometricStatusMessage(),
                                                    currentCurrency = currencySymbol,
                                                    storeName = storeName,
                                                    dailyTargetProfit = dailyTargetProfit,
                                                    salesCount = allSales.size,
                                                    onSetupPin = { viewModel.setupNewPin(it) },
                                                    onDisablePin = { viewModel.removePinSecurity() },
                                                    onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                                                    onToggleAutoLock = { viewModel.setAutoLockEnabled(it) },
                                                    onToggleHaptics = { viewModel.setHapticsEnabled(it) },
                                                    onSelectCurrency = { viewModel.setCurrency(it) },
                                                    onUpdateStoreName = { viewModel.setStoreName(it) },
                                                    onUpdateDailyTarget = { viewModel.setDailyTarget(it) },
                                                    onTestBiometrics = {
                                                        viewModel.triggerBiometricAuth(this@MainActivity, isManualTest = true)
                                                    },
                                                    onSuppressAutoLock = { viewModel.suppressAutoLockOnce() },
                                                    onSaveCsvToUri = { uri -> viewModel.saveExportToUri(uri, isCsv = true) },
                                                    onSaveJsonToUri = { uri -> viewModel.saveExportToUri(uri, isCsv = false) },
                                                    onShareFile = { isCsv ->
                                                        viewModel.createShareFileIntent(isCsv) { intent ->
                                                            startActivity(intent)
                                                        }
                                                    },
                                                    onImportFromUri = { uri -> viewModel.importFromUri(uri) },
                                                    onImportText = { raw, isCsv -> viewModel.importFromText(raw, isCsv) },
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
    }
}

@Composable
fun HisabTopBar(
    currentTab: AppTab,
    storeName: String = "My Store",
    isPinEnabled: Boolean,
    onLockClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite.copy(alpha = 0.94f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
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
                                .background(ProfitEmeraldBg)
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
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isPinEnabled) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLockClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle)
                        .border(1.dp, BorderHairline, CircleShape)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BorderHairline)
                .size(1.dp)
        )
    }
}

@Composable
fun HisabBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(1.dp, BorderHairline)
            .windowInsetsPadding(WindowInsets.navigationBars)
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
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

@Composable
fun HisabNavigationRail(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationRail(
        containerColor = SurfaceWhite,
        windowInsets = WindowInsets.statusBars.union(WindowInsets.navigationBars),
        modifier = Modifier.testTag("nav_rail")
    ) {
        Spacer(modifier = Modifier.weight(1f))
        AppTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationRailItem(
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
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = BrandDark,
                    selectedTextColor = BrandDark,
                    indicatorColor = SurfaceSubtle,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("rail_item_${tab.name.lowercase()}")
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
