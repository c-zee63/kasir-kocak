package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["createdAt"]),
        Index(value = ["isVoid"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val totalProfit: Double = 0.0,
    val paymentMethod: String = "cash",
    val amountPaid: Double = 0.0,
    val changeAmount: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val notes: String = "",
    val cashierName: String = "Kasir",
    val isVoid: Boolean = false,
    val voidReason: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
