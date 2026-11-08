package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val price: Double,
    val cost: Double,
    val profit: Double = price - cost,
    val marginPercent: Double = if (price > 0.0) ((price - cost) / price) * 100.0 else 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Int = 1,
    val note: String = ""
)
