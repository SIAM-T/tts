package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SaleEntity
import com.example.ui.TodaySummary
import com.example.ui.components.DailyEarningsSummaryHeader
import com.example.ui.components.LiveProfitBadge
import com.example.ui.components.bounceClick
import com.example.ui.components.formatMoney
import com.example.ui.components.formatNumber
import com.example.ui.components.performHapticTap
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.CostAmber
import com.example.ui.theme.LossRose
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBg
import com.example.ui.theme.ProfitEmeraldBorder
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.RevenueSapphireBg
import com.example.ui.theme.RevenueSapphireBorder
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun sanitizeDecimalInput(raw: String): String {
    val sb = StringBuilder()
    var hasDot = false
    for (ch in raw) {
        if (ch.isDigit()) {
            sb.append(ch)
        } else if (ch == '.' && !hasDot) {
            if (sb.isEmpty()) sb.append('0')
            sb.append('.')
            hasDot = true
        }
    }
    return sb.toString()
}

@Composable
fun QuickSaleScreen(
    todaySummary: TodaySummary,
    recentSales: List<SaleEntity>,
    recentTitles: List<String>,
    currencySymbol: String,
    storeName: String = "My Store",
    dailyTargetProfit: Double = 100.0,
    hapticsEnabled: Boolean = true,
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

    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
    val parsedCost = costText.toDoubleOrNull() ?: 0.0
    val hasAnyInput = title.isNotBlank() || priceText.isNotEmpty() || costText.isNotEmpty() || quantity > 1

    fun clearForm() {
        title = ""
        priceText = ""
        costText = ""
        quantity = 1
        focusManager.clearFocus()
    }

    fun submitSale() {
        if (parsedPrice > 0.0 || parsedCost > 0.0) {
            performHapticTap(context, hapticsEnabled, heavy = true)
            val net = (parsedPrice - parsedCost) * quantity
            lastRecordedProfit = net
            showSuccessToast = true

            onRecordSale(
                if (title.isBlank()) "Quick Item" else title,
                parsedPrice,
                parsedCost,
                quantity
            )

            clearForm()
        }
    }

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(3200)
            showSuccessToast = false
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isTabletWide = maxWidth >= 720.dp
        val isNarrowPhone = maxWidth < 360.dp

        val entrySection: @Composable () -> Unit = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DailyEarningsSummaryHeader(
                    summary = todaySummary,
                    currencySymbol = currencySymbol,
                    storeName = storeName,
                    dailyTargetProfit = dailyTargetProfit,
                    modifier = Modifier.testTag("today_earnings_header")
                )

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
                            .background(ProfitEmeraldBg)
                            .border(1.2.dp, ProfitEmeraldBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ProfitEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sale Recorded!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Text(
                                text = formatMoney(lastRecordedProfit, currencySymbol, showPlus = true),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lastRecordedProfit >= 0) ProfitEmerald else LossRose,
                                style = TabularNumberStyle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Field Quick Sale Entry Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceWhite,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderHairline, RoundedCornerShape(22.dp))
                        .testTag("quick_sale_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
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
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Instant profit & margin",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Quantity Selector Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceSubtle)
                                    .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Qty",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$quantity",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = TabularNumberStyle
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWhite)
                                        .bounceClick(enabled = quantity > 1) {
                                            performHapticTap(context, hapticsEnabled)
                                            if (quantity > 1) quantity--
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWhite)
                                        .bounceClick {
                                            performHapticTap(context, hapticsEnabled)
                                            quantity++
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Field 1: Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("1. Item Title") },
                            placeholder = { Text("e.g. Wireless Mouse, Milk, Shirt") },
                            trailingIcon = {
                                if (title.isNotEmpty()) {
                                    IconButton(onClick = { title = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear title",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
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
                                unfocusedBorderColor = BorderHairline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_title")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick title suggestion pills
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (title == chipText) RevenueSapphireBg else SurfaceSubtle)
                                        .border(
                                            1.dp,
                                            if (title == chipText) RevenueSapphireBorder else BorderHairline,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .bounceClick {
                                            performHapticTap(context, hapticsEnabled)
                                            title = chipText
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = chipText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (title == chipText) RevenueSapphire else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Field 2 & 3: Price & Cost (stack vertically on ultra-narrow phones)
                        val priceField: @Composable (Modifier) -> Unit = { fieldMod ->
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = sanitizeDecimalInput(it) },
                                label = { Text("2. Selling Price") },
                                prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = ProfitEmerald) },
                                placeholder = { Text("0.00") },
                                trailingIcon = {
                                    if (priceText.isNotEmpty()) {
                                        IconButton(onClick = { priceText = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear price",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TabularNumberStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(
                                            if (isNarrowPhone) FocusDirection.Down else FocusDirection.Right
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite,
                                    focusedBorderColor = ProfitEmerald,
                                    unfocusedBorderColor = BorderHairline
                                ),
                                modifier = fieldMod.testTag("input_price")
                            )
                        }

                        val costField: @Composable (Modifier) -> Unit = { fieldMod ->
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = sanitizeDecimalInput(it) },
                                label = { Text("3. Item Cost") },
                                prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = CostAmber) },
                                placeholder = { Text("0.00") },
                                trailingIcon = {
                                    if (costText.isNotEmpty()) {
                                        IconButton(onClick = { costText = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear cost",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TabularNumberStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
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
                                    unfocusedBorderColor = BorderHairline
                                ),
                                modifier = fieldMod.testTag("input_cost")
                            )
                        }

                        if (isNarrowPhone) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                priceField(Modifier.fillMaxWidth())
                                costField(Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                priceField(Modifier.weight(1f))
                                costField(Modifier.weight(1f))
                            }
                        }

                        // Smart Markup Helper Chips
                        AnimatedVisibility(visible = parsedCost > 0.0) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Markup:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    MarkupPresetChip("+30%") {
                                        performHapticTap(context, hapticsEnabled)
                                        priceText = String.format(Locale.US, "%.2f", parsedCost * 1.30)
                                    }
                                    MarkupPresetChip("+50%") {
                                        performHapticTap(context, hapticsEnabled)
                                        priceText = String.format(Locale.US, "%.2f", parsedCost * 1.50)
                                    }
                                    MarkupPresetChip("2x") {
                                        performHapticTap(context, hapticsEnabled)
                                        priceText = String.format(Locale.US, "%.2f", parsedCost * 2.0)
                                    }
                                    MarkupPresetChip("+${currencySymbol}5") {
                                        performHapticTap(context, hapticsEnabled)
                                        priceText = String.format(Locale.US, "%.2f", parsedCost + 5.0)
                                    }
                                    MarkupPresetChip("+${currencySymbol}10") {
                                        performHapticTap(context, hapticsEnabled)
                                        priceText = String.format(Locale.US, "%.2f", parsedCost + 10.0)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LiveProfitBadge(
                            price = parsedPrice * quantity,
                            cost = parsedCost * quantity,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.testTag("live_profit_badge")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasAnyInput) {
                                TextButton(
                                    onClick = {
                                        performHapticTap(context, hapticsEnabled)
                                        clearForm()
                                    },
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Reset", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                            }

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
                                    .weight(1f)
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
                                        fontSize = 15.sp,
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
                                                text = formatMoney(net, currencySymbol, showPlus = true),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SurfaceWhite,
                                                style = TabularNumberStyle
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

        val recentSection: @Composable () -> Unit = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recent Transactions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(onClick = onNavigateToLog) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "View All",
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

                Spacer(modifier = Modifier.height(6.dp))

                val latestItems = recentSales.take(if (isTabletWide) 8 else 5)
                if (latestItems.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderHairline, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No sales recorded yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enter Title, Price & Cost to calculate profit instantly",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        latestItems.forEach { sale ->
                            QuickSaleItemRow(
                                sale = sale,
                                currencySymbol = currencySymbol,
                                onDuplicate = {
                                    performHapticTap(context, hapticsEnabled)
                                    onDuplicateSale(sale)
                                },
                                onDelete = {
                                    performHapticTap(context, hapticsEnabled)
                                    onDeleteSale(sale)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isTabletWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    entrySection()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    recentSection()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.widthIn(max = 620.dp).fillMaxWidth()) {
                    entrySection()
                    Spacer(modifier = Modifier.height(20.dp))
                    recentSection()
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
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
            .clip(RoundedCornerShape(9.dp))
            .background(RevenueSapphireBg)
            .border(1.dp, RevenueSapphireBorder, RoundedCornerShape(9.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RevenueSapphire,
            style = TabularNumberStyle
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
    val isProfit = sale.profit >= 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (sale.quantity > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "x${sale.quantity}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                style = TabularNumberStyle
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "$timeStr • P: ${formatMoney(sale.price, currencySymbol)} • C: ${formatMoney(sale.cost, currencySymbol)}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMoney(sale.profit, currencySymbol, showPlus = true),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitEmerald else LossRose,
                        style = TabularNumberStyle
                    )
                    Text(
                        text = "${formatNumber(sale.marginPercent, 1)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isProfit) ProfitEmerald else LossRose,
                        style = TabularNumberStyle
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Sale",
                        tint = RevenueSapphire,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
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
