package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "Toko Saya",
    val storeAddress: String = "",
    val storePhone: String = "",
    val storeEmail: String = "",
    val storeLogoPath: String = "",
    val receiptFootnote: String = "Terima kasih telah berbelanja!",
    val currency: String = "IDR",
    val taxRate: Double = 0.0,
    val receiptPrefix: String = "KTQ",
    val lowStockThreshold: Int = 5,
    val createdAt: String = "",
    val updatedAt: String = ""
)
