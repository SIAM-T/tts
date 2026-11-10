package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SaleEntity
import com.example.ui.TodaySummary
import com.example.ui.components.DailyEarningsSummaryHeader
import com.example.ui.components.LiveProfitBadge
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuickSaleScreen(
    todaySummary: TodaySummary,
    recentSales: List<SaleEntity>,
    recentTitles: List<String>,
    currencySymbol: String,
    storeName: String = "My Store",
    dailyTargetProfit: Double = 100.0,
    onRecordSale: (title: String, price: Double, cost: Double, quantity: Int) -> Unit,
    onDuplicateSale: (SaleEntity) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var lastRecordedProfit by remember { mutableDoubleStateOf(0.0) }
    var showSuccessToast by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedCost = costText.toDoubleOrNull() ?: 0.0

    fun vibrate() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, 100))
            }
        } catch (_: Exception) { }
    }

    fun submitSale() {
        if (parsedPrice > 0.0 || parsedCost > 0.0) {
            vibrate()
            val net = (parsedPrice - parsedCost) * quantity
            lastRecordedProfit = net
            showSuccessToast = true

            onRecordSale(
                if (title.isBlank()) "Quick Item" else title,
                parsedPrice,
                parsedCost,
                quantity
            )

            // Reset form for next customer
            title = ""
            priceText = ""
            costText = ""
            quantity = 1
            focusManager.clearFocus()
        }
    }

    // Auto dismiss success toast after 3.5 seconds
    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(3500)
            showSuccessToast = false
        }
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
            // 1. Store Greeting & Today's Live Earnings Summary Header
            DailyEarningsSummaryHeader(
                summary = todaySummary,
                currencySymbol = currencySymbol,
                storeName = storeName,
                dailyTargetProfit = dailyTargetProfit,
                modifier = Modifier.testTag("today_earnings_header")
            )

            // Success animated notification banner
            AnimatedVisibility(
                visible = showSuccessToast,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.2.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ProfitEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sale Recorded Successfully!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                        Text(
                            text = "+$currencySymbol${String.format(Locale.US, "%.2f", lastRecordedProfit)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProfitEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Main 3-Field Quick Sale Entry Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_sale_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ProfitEmerald.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sell,
                                    contentDescription = null,
                                    tint = ProfitEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "3-Field Sale Entry",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Auto-calculates profit & margin",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Quantity Selector Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Qty: ",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$quantity",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceWhite)
                                    .clickable { if (quantity > 1) quantity-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceWhite)
                                    .clickable { quantity++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 1: Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("1. Item Title / Product Name") },
                        placeholder = { Text("e.g. Wireless Mouse, Cotton Shirt, Milk") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedBorderColor = RevenueSapphire,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title")
                    )

                    // Quick category / suggestion pills
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickPresets = listOf("General Item", "Grocery", "Electronics", "Snacks", "Clothing", "Service")
                        val displayChips = (recentTitles.take(3) + quickPresets).distinct().take(6)

                        displayChips.forEach { chipText ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceSubtle)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { title = chipText }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = chipText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2 & 3: Selling Price & Item Cost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Field 2: Selling Price
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("2. Price ($currencySymbol)") },
                            placeholder = { Text("0.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Right) }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedBorderColor = ProfitEmerald,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_price")
                        )

                        // Field 3: Cost Price
                        OutlinedTextField(
                            value = costText,
                            onValueChange = { costText = it },
                            label = { Text("3. Cost ($currencySymbol)") },
                            placeholder = { Text("0.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { submitSale() }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedBorderColor = CostAmber,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_cost")
                        )
                    }

                    // Smart Markup Helper Chips (When cost is entered, tap to auto-set price!)
                    if (parsedCost > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Markup: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            // +30%
                            MarkupPresetChip("+30%") {
                                val newP = parsedCost * 1.30
                                priceText = String.format(Locale.US, "%.2f", newP)
                            }
                            // +50%
                            MarkupPresetChip("+50%") {
                                val newP = parsedCost * 1.50
                                priceText = String.format(Locale.US, "%.2f", newP)
                            }
                            // 2x Markup
                            MarkupPresetChip("2x (100%)") {
                                val newP = parsedCost * 2.0
                                priceText = String.format(Locale.US, "%.2f", newP)
                            }
                            // +$5
                            MarkupPresetChip("+$5") {
                                val newP = parsedCost + 5.0
                                priceText = String.format(Locale.US, "%.2f", newP)
                            }
                            // +$10
                            MarkupPresetChip("+$10") {
                                val newP = parsedCost + 10.0
                                priceText = String.format(Locale.US, "%.2f", newP)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live auto-calculated profit & margin pill
                    LiveProfitBadge(
                        price = parsedPrice * quantity,
                        cost = parsedCost * quantity,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.testTag("live_profit_badge")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action Button: Record Sale
                    Button(
                        onClick = { submitSale() },
                        enabled = parsedPrice > 0.0 || parsedCost > 0.0,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandDark,
                            contentColor = SurfaceWhite,
                            disabledContainerColor = SurfaceSubtle,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("record_sale_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Record Sale",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (parsedPrice > 0) {
                                val net = (parsedPrice - parsedCost) * quantity
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (net >= 0) ProfitEmerald else LossRose)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${if (net >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", net)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SurfaceWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Today's Recent Sales list header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Today's Recent Transactions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                TextButton(onClick = onNavigateToLog) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Log",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RevenueSapphire
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = RevenueSapphire,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recent sales items
            val todayRecent = recentSales.take(5)
            if (todayRecent.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No sales recorded yet today",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter price & cost above for instant profit calculation",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayRecent.forEach { sale ->
                        QuickSaleItemRow(
                            sale = sale,
                            currencySymbol = currencySymbol,
                            onDuplicate = { onDuplicateSale(sale) },
                            onDelete = { onDeleteSale(sale) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MarkupPresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RevenueSapphire
        )
    }
}

@Composable
private fun QuickSaleItemRow(
    sale: SaleEntity,
    currencySymbol: String,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val timeStr = sdf.format(Date(sale.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (sale.quantity > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "x${sale.quantity}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Price: $currencySymbol${String.format(Locale.US, "%.2f", sale.price)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cost: $currencySymbol${String.format(Locale.US, "%.2f", sale.cost)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val isProfit = sale.profit >= 0
                    Text(
                        text = "${if (isProfit) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", sale.profit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitEmerald else LossRose
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", sale.marginPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isProfit) ProfitEmerald else LossRose
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Duplicate Button
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Sale",
                        tint = RevenueSapphire,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Sale",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
