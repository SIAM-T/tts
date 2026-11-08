package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuickSaleScreen(
    todaySummary: TodaySummary,
    recentSales: List<SaleEntity>,
    recentTitles: List<String>,
    currencySymbol: String,
    onRecordSale: (title: String, price: Double, cost: Double, quantity: Int) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var showSuccessFeedback by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedCost = costText.toDoubleOrNull() ?: 0.0

    fun submitSale() {
        if (parsedPrice > 0.0 || parsedCost > 0.0) {
            onRecordSale(
                if (title.isBlank()) "Quick Item" else title,
                parsedPrice,
                parsedCost,
                quantity
            )
            title = ""
            priceText = ""
            costText = ""
            quantity = 1
            focusManager.clearFocus()
            showSuccessFeedback = true
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
            // 1. Live Daily Earnings Summary Header
            DailyEarningsSummaryHeader(
                summary = todaySummary,
                currencySymbol = currencySymbol,
                modifier = Modifier.testTag("today_earnings_header")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Main 3-Field Quick Sale Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.2.dp, brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
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
                                    .size(32.dp)
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
                            Text(
                                text = "New Sale Entry",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Quantity selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Qty: ",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$quantity",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
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
                        label = { Text("Item Title / Product Name") },
                        placeholder = { Text("e.g. Wireless Mouse, T-Shirt, Coffee") },
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

                    // Recent title suggestion chips
                    if (recentTitles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recentTitles.take(5).forEach { recent ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { title = recent }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = recent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2 & 3: Selling Price & Cost Price side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Field 2: Selling Price
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Selling Price ($currencySymbol)") },
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
                            label = { Text("Item Cost ($currencySymbol)") },
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
private fun QuickSaleItemRow(
    sale: SaleEntity,
    currencySymbol: String,
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

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Sale",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
