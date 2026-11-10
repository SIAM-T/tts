package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PinSetupDialog
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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isPinEnabled: Boolean,
    isBiometricEnabled: Boolean,
    canUseBiometrics: Boolean,
    currentCurrency: String,
    storeName: String,
    dailyTargetProfit: Double,
    onSetupPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSelectCurrency: (String) -> Unit,
    onUpdateStoreName: (String) -> Unit,
    onUpdateDailyTarget: (Double) -> Unit,
    onTestBiometrics: () -> Unit,
    onExportCsv: suspend () -> String,
    onExportJson: suspend () -> String,
    onImportCsv: (String) -> Unit,
    onImportJson: (String) -> Unit,
    onSeedSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importType by remember { mutableStateOf("CSV") }
    var importRawText by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showSeedConfirmation by remember { mutableStateOf(false) }

    var editingStoreName by remember { mutableStateOf(storeName) }
    var showStoreNameEdit by remember { mutableStateOf(false) }

    var editingDailyTarget by remember { mutableStateOf(dailyTargetProfit.toInt().toString()) }
    var showTargetEdit by remember { mutableStateOf(false) }

    fun shareText(title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, "Export Store Data"))
    }

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
            // Header
            Text(
                text = "Store Settings & Controls",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Security, store customization & offline backup",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Store Profile & Goals
            SettingsSectionCard(title = "Store Profile & Daily Goals") {
                // Store Name Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RevenueSapphire.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = RevenueSapphire,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Store Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(storeName, fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    TextButton(onClick = {
                        editingStoreName = storeName
                        showStoreNameEdit = true
                    }) {
                        Text("Edit", fontSize = 12.sp, color = RevenueSapphire)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Profit Target Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ProfitEmerald.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = ProfitEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Daily Profit Target", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("$currentCurrency${dailyTargetProfit.toInt()}/day target", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    TextButton(onClick = {
                        editingDailyTarget = dailyTargetProfit.toInt().toString()
                        showTargetEdit = true
                    }) {
                        Text("Change", fontSize = 12.sp, color = ProfitEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. PIN & Biometric Security
            SettingsSectionCard(title = "Security & Biometrics") {
                // PIN row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RevenueSapphire.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPinEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = RevenueSapphire,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App 4-Digit PIN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isPinEnabled) "PIN lock active on app launch" else "Protect app on startup",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showPinSetupDialog = true
                            } else {
                                onDisablePin()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ProfitEmerald
                        )
                    )
                }

                if (isPinEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPinSetupDialog = true }) {
                            Text("Change PIN", fontSize = 12.sp, color = RevenueSapphire)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { onDisablePin() }) {
                            Text("Turn Off", fontSize = 12.sp, color = LossRose)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Biometrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ProfitEmerald.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = ProfitEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Biometric Unlock",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (canUseBiometrics) "Fingerprint / Face recognition" else "Hardware check ready",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { onToggleBiometric(it) },
                        enabled = isPinEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ProfitEmerald
                        )
                    )
                }

                if (isBiometricEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onTestBiometrics,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSubtle, contentColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = ProfitEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Biometric Prompt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Currency Preference
            SettingsSectionCard(title = "Currency Symbol") {
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandDark else SurfaceSubtle)
                                .border(
                                    1.dp,
                                    if (isSelected) BrandDark else BorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectCurrency(sym) },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Offline Data Backup & Restore
            SettingsSectionCard(title = "Offline Backup & Export") {
                Text(
                    text = "Hisab stores 100% of your data offline on device. You can export or import your records at any time.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = onExportCsv()
                                shareText("Hisab Store Sales CSV Backup", csv)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = onExportJson()
                                shareText("Hisab Store Sales JSON Backup", json)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RevenueSapphire),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            importType = "CSV"
                            importRawText = ""
                            showImportDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSubtle, contentColor = TextPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import CSV", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            importType = "JSON"
                            importRawText = ""
                            showImportDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSubtle, contentColor = TextPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Data Management & Demo Seeding
            SettingsSectionCard(title = "Data Management & Tools") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showSeedConfirmation = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RevenueSapphire.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = RevenueSapphire, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seed Realistic Demo Store Sales", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Populates sample items to test charts & analytics", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showClearConfirmation = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LossRose.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = LossRose, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear All Sales Data", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LossRose)
                        Text("Reset database back to clean empty state", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // PIN Setup / Change Full-Screen Sheet
    if (showPinSetupDialog) {
        PinSetupDialog(
            onDismiss = { showPinSetupDialog = false },
            onPinConfigured = { pin ->
                onSetupPin(pin)
                showPinSetupDialog = false
            }
        )
    }

    // Store Name Edit Dialog
    if (showStoreNameEdit) {
        AlertDialog(
            onDismissRequest = { showStoreNameEdit = false },
            title = { Text("Set Store Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingStoreName,
                    onValueChange = { editingStoreName = it },
                    label = { Text("Store Name") },
                    placeholder = { Text("e.g. Apex Electronics, Corner Market") },
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
                TextButton(onClick = { showStoreNameEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Daily Target Profit Edit Dialog
    if (showTargetEdit) {
        AlertDialog(
            onDismissRequest = { showTargetEdit = false },
            title = { Text("Daily Profit Goal ($currentCurrency)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingDailyTarget,
                    onValueChange = { editingDailyTarget = it },
                    label = { Text("Target Profit Amount") },
                    placeholder = { Text("e.g. 150") },
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
                TextButton(onClick = { showTargetEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Dialog (paste CSV or JSON)
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import $importType Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste your $importType content below. Records will be added to your offline database.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importRawText,
                        onValueChange = { importRawText = it },
                        placeholder = { Text("Paste $importType text here...") },
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importType == "CSV") {
                            onImportCsv(importRawText)
                        } else {
                            onImportJson(importRawText)
                        }
                        showImportDialog = false
                    },
                    enabled = importRawText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                ) {
                    Text("Import Now", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Sales?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all recorded transactions from the local device database. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRose)
                ) {
                    Text("Yes, Clear Everything", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Seed Confirmation Dialog
    if (showSeedConfirmation) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmation = false },
            title = { Text("Load Sample Store Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will add 13 realistic retail store sales across the last 7 days so you can test and view charts, margin distributions, and financial reports.") },
            confirmButton = {
                Button(
                    onClick = {
                        onSeedSampleData()
                        showSeedConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                ) {
                    Text("Load Sample Sales", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
