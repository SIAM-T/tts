package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SaleEntity
import com.example.ui.MarginFilterTier
import com.example.ui.SortOrder
import com.example.ui.TimeFilter
import com.example.ui.components.bounceClick
import com.example.ui.components.formatMoney
import com.example.ui.components.formatNumber
import com.example.ui.components.performHapticTap
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BrandDark
import com.example.ui.theme.CostAmber
import com.example.ui.theme.LossRose
import com.example.ui.theme.LossRoseBg
import com.example.ui.theme.ProfitEmerald
import com.example.ui.theme.ProfitEmeraldBg
import com.example.ui.theme.RevenueSapphire
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SalesLogScreen(
    sales: List<SaleEntity>,
    timeFilter: TimeFilter,
    marginFilter: MarginFilterTier,
    sortOrder: SortOrder = SortOrder.NEWEST,
    searchQuery: String,
    selectedSpecificDateMillis: Long?,
    currencySymbol: String,
    hapticsEnabled: Boolean = true,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onMarginFilterChange: (MarginFilterTier) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onSelectSpecificDate: (Long?) -> Unit,
    onDuplicateSale: (SaleEntity) -> Unit = {},
    onUpdateSale: (SaleEntity) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingSale by remember { mutableStateOf<SaleEntity?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sdfDateOnly = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val sdfDateTime = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US) }

    val totalRevenue = sales.sumOf { it.price * it.quantity }
    val totalCost = sales.sumOf { it.cost * it.quantity }
    val totalProfit = totalRevenue - totalCost
    val marginPct = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100.0 else 0.0

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        if (selectedSpecificDateMillis != null) {
            cal.timeInMillis = selectedSpecificDateMillis
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSelectSpecificDate(pickedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val useTwoColumnGrid = maxWidth >= 700.dp
        val isNarrow = maxWidth < 360.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (useTwoColumnGrid) 20.dp else 16.dp, vertical = 12.dp)
        ) {
            // 1. Search Bar & Date Picker & Sort Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search items...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = RevenueSapphire,
                        unfocusedBorderColor = BorderHairline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_sales_input")
                )

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedSpecificDateMillis != null) BrandDark else SurfaceWhite)
                        .border(
                            1.dp,
                            if (selectedSpecificDateMillis != null) BrandDark else BorderHairline,
                            RoundedCornerShape(14.dp)
                        )
                        .bounceClick {
                            performHapticTap(context, hapticsEnabled)
                            openDatePicker()
                        }
                        .testTag("date_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Filter by Specific Date",
                        tint = if (selectedSpecificDateMillis != null) SurfaceWhite else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceWhite)
                            .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                            .bounceClick {
                                performHapticTap(context, hapticsEnabled)
                                showSortMenu = true
                            }
                            .testTag("sort_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Transactions",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.label,
                                        fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sortOrder == order) RevenueSapphire else TextPrimary
                                    )
                                },
                                onClick = {
                                    onSortOrderChange(order)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Time Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedSpecificDateMillis != null) {
                    val formattedDate = sdfDateOnly.format(Date(selectedSpecificDateMillis))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandDark)
                            .clickable { onSelectSpecificDate(null) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Date Filter",
                                tint = SurfaceWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                TimeFilter.entries.forEach { filter ->
                    val isSelected = selectedSpecificDateMillis == null && timeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) RevenueSapphire else SurfaceWhite)
                            .border(
                                1.dp,
                                if (isSelected) RevenueSapphire else BorderHairline,
                                RoundedCornerShape(10.dp)
                            )
                            .bounceClick {
                                performHapticTap(context, hapticsEnabled)
                                onSelectSpecificDate(null)
                                onTimeFilterChange(filter)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SurfaceWhite else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Margin Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarginFilterTier.entries.forEach { tier ->
                    val isSelected = marginFilter == tier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) ProfitEmeraldBg else SurfaceSubtle)
                            .border(
                                1.dp,
                                if (isSelected) ProfitEmerald else Color.Transparent,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { onMarginFilterChange(tier) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tier.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ProfitEmerald else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Filtered Period Summary Banner (responsive so big numbers never clip or collide)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
            ) {
                if (isNarrow) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryMetricCell("SALES", "${sales.size}", TextPrimary, Modifier.weight(1f))
                            SummaryMetricCell("REVENUE", formatMoney(totalRevenue, currencySymbol), RevenueSapphire, Modifier.weight(1f), Alignment.End)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryMetricCell("NET PROFIT", formatMoney(totalProfit, currencySymbol, showPlus = true), if (totalProfit >= 0) ProfitEmerald else LossRose, Modifier.weight(1f))
                            SummaryMetricCell("MARGIN", "${formatNumber(marginPct, 1)}%", if (totalProfit >= 0) ProfitEmerald else LossRose, Modifier.weight(1f), Alignment.End)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryMetricCell("SALES", "${sales.size}", TextPrimary, Modifier.weight(0.65f))
                        SummaryMetricCell("REVENUE", formatMoney(totalRevenue, currencySymbol), RevenueSapphire, Modifier.weight(1.1f))
                        SummaryMetricCell("NET PROFIT", formatMoney(totalProfit, currencySymbol, showPlus = true), if (totalProfit >= 0) ProfitEmerald else LossRose, Modifier.weight(1.1f))
                        SummaryMetricCell("MARGIN", "${formatNumber(marginPct, 1)}%", if (totalProfit >= 0) ProfitEmerald else LossRose, Modifier.weight(0.75f), Alignment.End)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Sales Transactions List / Grid
            if (sales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No sales match this filter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching dates, clearing search, or adding a sale",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else if (useTwoColumnGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItems(sales, key = { it.id }) { sale ->
                        DetailedSaleCard(
                            sale = sale,
                            currencySymbol = currencySymbol,
                            dateTimeFormatted = sdfDateTime.format(Date(sale.timestamp)),
                            onDuplicate = {
                                performHapticTap(context, hapticsEnabled)
                                onDuplicateSale(sale)
                            },
                            onEdit = { editingSale = sale },
                            onDelete = { saleToDelete = sale },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales, key = { it.id }) { sale ->
                        DetailedSaleCard(
                            sale = sale,
                            currencySymbol = currencySymbol,
                            dateTimeFormatted = sdfDateTime.format(Date(sale.timestamp)),
                            onDuplicate = {
                                performHapticTap(context, hapticsEnabled)
                                onDuplicateSale(sale)
                            },
                            onEdit = { editingSale = sale },
                            onDelete = { saleToDelete = sale },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    editingSale?.let { sale ->
        EditSaleDialog(
            sale = sale,
            currencySymbol = currencySymbol,
            onDismiss = { editingSale = null },
            onSave = { updated ->
                onUpdateSale(updated)
                editingSale = null
            }
        )
    }

    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Sale", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${sale.title}'? Store totals will be recalculated immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(sale)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRose)
                ) {
                    Text("Delete", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SummaryMetricCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            style = TabularNumberStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailedSaleCard(
    sale: SaleEntity,
    currencySymbol: String,
    dateTimeFormatted: String,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = sale.profit >= 0

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(18.dp))
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateTimeFormatted,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isProfit) ProfitEmeraldBg else LossRoseBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${formatNumber(sale.marginPercent, 1)}% Margin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitEmerald else LossRose,
                        style = TabularNumberStyle
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceSubtle)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PRICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = formatMoney(sale.price, currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        style = TabularNumberStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("COST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = formatMoney(sale.cost, currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CostAmber,
                        style = TabularNumberStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(modifier = Modifier.weight(0.55f)) {
                    Text("QTY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = "${sale.quantity}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        style = TabularNumberStyle
                    )
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                    Text("NET PROFIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = formatMoney(sale.profit, currencySymbol, showPlus = true),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isProfit) ProfitEmerald else LossRose,
                        style = TabularNumberStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (sale.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${sale.note}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Sale",
                        tint = RevenueSapphire,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Sale",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Sale",
                        tint = LossRose,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditSaleDialog(
    sale: SaleEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (SaleEntity) -> Unit
) {
    var title by remember { mutableStateOf(sale.title) }
    var priceText by remember { mutableStateOf(sale.price.toString()) }
    var costText by remember { mutableStateOf(sale.cost.toString()) }
    var quantityText by remember { mutableStateOf(sale.quantity.toString()) }
    var note by remember { mutableStateOf(sale.note) }

    val p = priceText.toDoubleOrNull() ?: sale.price
    val c = costText.toDoubleOrNull() ?: sale.cost
    val q = (quantityText.toIntOrNull() ?: sale.quantity).coerceAtLeast(1)
    val previewProfit = (p - c) * q

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Sale", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { raw ->
                            if (raw.all { it.isDigit() || it == '.' }) priceText = raw
                        },
                        label = { Text("Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { raw ->
                            if (raw.all { it.isDigit() || it == '.' }) costText = raw
                        },
                        label = { Text("Cost ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { raw ->
                        if (raw.all { it.isDigit() }) quantityText = raw
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Updated Profit:", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = formatMoney(previewProfit, currencySymbol, showPlus = true),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (previewProfit >= 0) ProfitEmerald else LossRose,
                        style = TabularNumberStyle
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = sale.copy(
                        title = title.ifBlank { "Sale" },
                        price = p,
                        cost = c,
                        quantity = q,
                        profit = (p - c) * q,
                        marginPercent = if (p > 0) ((p - c) / p) * 100.0 else 0.0,
                        note = note.trim()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
            ) {
                Text("Save", color = SurfaceWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
