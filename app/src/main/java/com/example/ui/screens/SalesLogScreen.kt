package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SaleEntity
import com.example.ui.MarginFilterTier
import com.example.ui.TimeFilter
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SalesLogScreen(
    sales: List<SaleEntity>,
    timeFilter: TimeFilter,
    marginFilter: MarginFilterTier,
    searchQuery: String,
    selectedSpecificDateMillis: Long?,
    currencySymbol: String,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onMarginFilterChange: (MarginFilterTier) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectSpecificDate: (Long?) -> Unit,
    onUpdateSale: (SaleEntity) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingSale by remember { mutableStateOf<SaleEntity?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleEntity?>(null) }

    val sdfDateOnly = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val sdfDateTime = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US) }

    // Summary calculations for currently viewed filter
    val totalRevenue = sales.sumOf { it.price * it.quantity }
    val totalCost = sales.sumOf { it.cost * it.quantity }
    val totalProfit = totalRevenue - totalCost
    val marginPct = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100.0 else 0.0

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        if (selectedSpecificDateMillis != null) {
            cal.timeInMillis = selectedSpecificDateMillis
        }
        val dialog = DatePickerDialog(
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
        )
        dialog.show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Search Bar & Date Picker Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by item title...", fontSize = 13.sp) },
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
                                    contentDescription = "Clear",
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
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_sales_input")
                )

                // Calendar date picker toggle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedSpecificDateMillis != null) BrandDark else SurfaceWhite)
                        .border(
                            1.dp,
                            if (selectedSpecificDateMillis != null) BrandDark else BorderSubtle,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { openDatePicker() }
                        .testTag("date_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick Date",
                        tint = if (selectedSpecificDateMillis != null) SurfaceWhite else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Time Filter Chips Row (Today, Yesterday, 7D, 30D, All Time, or Active Date)
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
                                text = "Date: $formattedDate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Date",
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
                                if (isSelected) RevenueSapphire else BorderSubtle,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
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

            // 3. Margin Filter Chips (All, High, Moderate, Low, Loss)
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ProfitEmerald.copy(alpha = 0.15f) else SurfaceSubtle)
                            .border(
                                1.dp,
                                if (isSelected) ProfitEmerald else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onMarginFilterChange(tier) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tier.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ProfitEmerald else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Filtered Period Quick Metrics Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("${sales.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", totalRevenue)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RevenueSapphire)
                    }
                    Column {
                        Text("NET PROFIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(
                            text = "${if (totalProfit >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", totalProfit)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalProfit >= 0) ProfitEmerald else LossRose
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("MARGIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("${String.format(Locale.US, "%.1f", marginPct)}%", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (totalProfit >= 0) ProfitEmerald else LossRose)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Sales Transactions List
            if (sales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No sales match this criteria",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching time filters or recording a sale",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales, key = { it.id }) { sale ->
                        DetailedSaleCard(
                            sale = sale,
                            currencySymbol = currencySymbol,
                            dateTimeFormatted = sdfDateTime.format(Date(sale.timestamp)),
                            onEdit = { editingSale = sale },
                            onDelete = { saleToDelete = sale }
                        )
                    }
                }
            }
        }
    }

    // Edit Sale Dialog
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

    // Delete Confirmation Dialog
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Sale", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${sale.title}'? This will permanently update store revenue calculations.") },
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
private fun DetailedSaleCard(
    sale: SaleEntity,
    currencySymbol: String,
    dateTimeFormatted: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isProfit = sale.profit >= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
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
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateTimeFormatted,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Profit Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isProfit) ProfitEmerald.copy(alpha = 0.12f) else LossRose.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", sale.marginPercent)}% Margin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitEmerald else LossRose
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial breakdown row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceSubtle)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PRICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.price)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column {
                    Text("COST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("$currencySymbol${String.format(Locale.US, "%.2f", sale.cost)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CostAmber)
                }
                Column {
                    Text("QTY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("${sale.quantity}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("NET PROFIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(
                        text = "${if (isProfit) "+" else ""}$currencySymbol${String.format(Locale.US, "%.2f", sale.profit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isProfit) ProfitEmerald else LossRose
                    )
                }
            }

            // Optional note
            if (sale.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${sale.note}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action row (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = RevenueSapphire, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = LossRose, modifier = Modifier.size(16.dp))
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
                        onValueChange = { priceText = it },
                        label = { Text("Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Cost ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: sale.price
                    val c = costText.toDoubleOrNull() ?: sale.cost
                    val q = quantityText.toIntOrNull() ?: sale.quantity
                    val updated = sale.copy(
                        title = title.ifBlank { "Sale" },
                        price = p,
                        cost = c,
                        quantity = q.coerceAtLeast(1),
                        profit = (p - c) * q.coerceAtLeast(1),
                        marginPercent = if (p > 0) ((p - c) / p) * 100.0 else 0.0,
                        note = note
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
            ) {
                Text("Save Changes", color = SurfaceWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
