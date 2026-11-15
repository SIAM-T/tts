package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PinSetupDialog
import com.example.ui.components.bounceClick
import com.example.ui.components.formatMoney
import com.example.ui.components.performHapticTap
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    isPinEnabled: Boolean,
    isBiometricEnabled: Boolean,
    isAutoLockEnabled: Boolean,
    isHapticsEnabled: Boolean,
    canUseBiometrics: Boolean,
    biometricStatusText: String,
    currentCurrency: String,
    storeName: String,
    dailyTargetProfit: Double,
    salesCount: Int,
    onSetupPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleAutoLock: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectCurrency: (String) -> Unit,
    onUpdateStoreName: (String) -> Unit,
    onUpdateDailyTarget: (Double) -> Unit,
    onTestBiometrics: () -> Unit,
    onSuppressAutoLock: () -> Unit,
    onSaveCsvToUri: (Uri) -> Unit,
    onSaveJsonToUri: (Uri) -> Unit,
    onShareFile: (isCsv: Boolean) -> Unit,
    onImportFromUri: (Uri) -> Unit,
    onImportText: (raw: String, isCsv: Boolean) -> Unit,
    onSeedSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPasteImportDialog by remember { mutableStateOf(false) }
    var pasteImportIsCsv by remember { mutableStateOf(true) }
    var pasteImportText by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showSeedConfirmation by remember { mutableStateOf(false) }

    var editingStoreName by remember { mutableStateOf(storeName) }
    var showStoreNameEdit by remember { mutableStateOf(false) }

    var editingDailyTarget by remember { mutableStateOf(dailyTargetProfit.toInt().toString()) }
    var showTargetEdit by remember { mutableStateOf(false) }

    // Real File Save Launchers (Storage Access Framework)
    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { onSaveCsvToUri(it) }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onSaveJsonToUri(it) }
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImportFromUri(it) }
    }

    fun defaultFileName(ext: String): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "Hisab_Sales_$stamp.$ext"
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 740.dp

        val storeProfileCard: @Composable () -> Unit = {
            SettingsSectionCard(title = "Store Profile & Preferences") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIconBadge(Icons.Default.Store, RevenueSapphire)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Store Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = storeName,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    TextButton(onClick = {
                        editingStoreName = storeName
                        showStoreNameEdit = true
                    }) {
                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RevenueSapphire)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderHairline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIconBadge(Icons.Default.Flag, ProfitEmerald)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Daily Profit Goal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = "${formatMoney(dailyTargetProfit, currentCurrency)} / day",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                style = TabularNumberStyle
                            )
                        }
                    }

                    TextButton(onClick = {
                        editingDailyTarget = dailyTargetProfit.toInt().toString()
                        showTargetEdit = true
                    }) {
                        Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfitEmerald)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderHairline)

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIconBadge(Icons.Default.Vibration, BrandDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Haptic Feedback", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Tactile tap feedback on sales & PIN", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = {
                            onToggleHaptics(it)
                            if (it) performHapticTap(context, true)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ProfitEmerald
                        ),
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderHairline)

                Text(
                    text = "Currency Symbol",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currencies = listOf("$", "€", "£", "৳", "₹", "¥", "₱", "R$", "₦", "د.إ", "₩", "₺")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { sym ->
                        val isSelected = currentCurrency == sym
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandDark else SurfaceSubtle)
                                .border(
                                    1.dp,
                                    if (isSelected) BrandDark else BorderHairline,
                                    RoundedCornerShape(12.dp)
                                )
                                .bounceClick {
                                    performHapticTap(context, isHapticsEnabled)
                                    onSelectCurrency(sym)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SurfaceWhite else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        val securityCard: @Composable () -> Unit = {
            SettingsSectionCard(title = "Security & App Lock") {
                // PIN Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIconBadge(
                            if (isPinEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            RevenueSapphire
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("App 4-Digit PIN", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = if (isPinEnabled) "PIN protection is active" else "Protect store records with PIN",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) showPinSetupDialog = true else onDisablePin()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ProfitEmerald
                        ),
                        modifier = Modifier.testTag("switch_pin")
                    )
                }

                AnimatedVisibility(visible = isPinEnabled) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPinSetupDialog = true }) {
                                Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RevenueSapphire)
                            }
                            TextButton(onClick = onDisablePin) {
                                Text("Disable PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LossRose)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderHairline)

                        // Auto-Lock on Background Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                SettingsIconBadge(Icons.Default.LockClock, BrandDark)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Auto-Lock in Background", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Require PIN when switching back to app", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Switch(
                                checked = isAutoLockEnabled,
                                onCheckedChange = onToggleAutoLock,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SurfaceWhite,
                                    checkedTrackColor = ProfitEmerald
                                ),
                                modifier = Modifier.testTag("switch_autolock")
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderHairline)

                // Biometric Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        SettingsIconBadge(Icons.Default.Fingerprint, ProfitEmerald)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Biometric Unlock", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = if (!isPinEnabled) "Enable PIN first to unlock with biometrics" else biometricStatusText,
                                fontSize = 11.sp,
                                color = if (canUseBiometrics) TextSecondary else LossRose
                            )
                        }
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onToggleBiometric,
                        enabled = isPinEnabled && canUseBiometrics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ProfitEmerald
                        ),
                        modifier = Modifier.testTag("switch_biometric")
                    )
                }

                if (isPinEnabled && canUseBiometrics) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onTestBiometrics,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = ProfitEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Biometric Unlock", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }
        }

        val backupCard: @Composable () -> Unit = {
            SettingsSectionCard(title = "File Backup & Restore ($salesCount records)") {
                Text(
                    text = "Export real .csv or .json files to Downloads/Drive for large datasets, or import a backup file directly from device storage.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Save-to-File Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            performHapticTap(context, isHapticsEnabled)
                            onSuppressAutoLock()
                            saveCsvLauncher.launch(defaultFileName("csv"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_csv_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save .CSV File", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            performHapticTap(context, isHapticsEnabled)
                            onSuppressAutoLock()
                            saveJsonLauncher.launch(defaultFileName("json"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RevenueSapphire),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_json_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save .JSON File", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Share File Attachment Actions (FileProvider EXTRA_STREAM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            performHapticTap(context, isHapticsEnabled)
                            onShareFile(true)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_csv_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = BrandDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share .CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDark)
                    }

                    OutlinedButton(
                        onClick = {
                            performHapticTap(context, isHapticsEnabled)
                            onShareFile(false)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_json_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = RevenueSapphire, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share .JSON", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RevenueSapphire)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Import from File Picker
                Button(
                    onClick = {
                        performHapticTap(context, isHapticsEnabled)
                        onSuppressAutoLock()
                        openFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/json", "text/plain", "*/*"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceSubtle, contentColor = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("import_file_button")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = ProfitEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import .CSV or .JSON File", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        pasteImportText = ""
                        pasteImportIsCsv = true
                        showPasteImportDialog = true
                    }) {
                        Text("Or paste text snippet", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        val dataToolsCard: @Composable () -> Unit = {
            SettingsSectionCard(title = "Database Tools") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showSeedConfirmation = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBadge(Icons.Default.Refresh, RevenueSapphire)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Load Demo Store Sales", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Add 13 sample sales across 6 days to explore analytics", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = BorderHairline)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showClearConfirmation = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBadge(Icons.Default.DeleteForever, LossRose)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear All Sales Data", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LossRose)
                        Text("Permanently erase local database records", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWide) 20.dp else 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth()) {
                Text(
                    text = "Store Settings & Controls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Offline security, preferences & file backup",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            storeProfileCard()
                            dataToolsCard()
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            securityCard()
                            backupCard()
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        storeProfileCard()
                        securityCard()
                        backupCard()
                        dataToolsCard()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            hapticsEnabled = isHapticsEnabled,
            onDismiss = { showPinSetupDialog = false },
            onPinConfigured = { pin ->
                onSetupPin(pin)
                showPinSetupDialog = false
            }
        )
    }

    if (showStoreNameEdit) {
        AlertDialog(
            onDismissRequest = { showStoreNameEdit = false },
            title = { Text("Store Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingStoreName,
                    onValueChange = { editingStoreName = it },
                    label = { Text("Store Name") },
                    placeholder = { Text("e.g. Apex Electronics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStoreName(editingStoreName)
                        showStoreNameEdit = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoreNameEdit = false }) { Text("Cancel") }
            }
        )
    }

    if (showTargetEdit) {
        AlertDialog(
            onDismissRequest = { showTargetEdit = false },
            title = { Text("Daily Profit Goal ($currentCurrency)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingDailyTarget,
                    onValueChange = { raw ->
                        if (raw.all { it.isDigit() || it == '.' }) editingDailyTarget = raw
                    },
                    label = { Text("Target Profit Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = editingDailyTarget.toDoubleOrNull() ?: dailyTargetProfit
                        onUpdateDailyTarget(parsed)
                        showTargetEdit = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfitEmerald)
                ) {
                    Text("Save Goal", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetEdit = false }) { Text("Cancel") }
            }
        )
    }

    if (showPasteImportDialog) {
        AlertDialog(
            onDismissRequest = { showPasteImportDialog = false },
            title = { Text("Paste Backup Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (pasteImportIsCsv) BrandDark else SurfaceSubtle)
                                .clickable { pasteImportIsCsv = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (pasteImportIsCsv) Color.White else TextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!pasteImportIsCsv) RevenueSapphire else SurfaceSubtle)
                                .clickable { pasteImportIsCsv = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!pasteImportIsCsv) Color.White else TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pasteImportText,
                        onValueChange = { pasteImportText = it },
                        placeholder = { Text("Paste ${if (pasteImportIsCsv) "CSV" else "JSON"} text...") },
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportText(pasteImportText, pasteImportIsCsv)
                        showPasteImportDialog = false
                    },
                    enabled = pasteImportText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                ) {
                    Text("Import", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Sales?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently erase all $salesCount recorded transactions from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRose)
                ) {
                    Text("Clear Everything", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showSeedConfirmation) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmation = false },
            title = { Text("Load Sample Store Data?", fontWeight = FontWeight.Bold) },
            text = { Text("Adds 13 realistic store sales across the past week so you can explore trends and reports immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        onSeedSampleData()
                        showSeedConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                ) {
                    Text("Load Samples", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsIconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
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
            Text(
                text = title.uppercase(Locale.US),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
