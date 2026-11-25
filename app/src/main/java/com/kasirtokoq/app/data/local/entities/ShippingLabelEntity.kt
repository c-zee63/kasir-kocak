package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipping_labels",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["status"]),
        Index(value = ["trackingNumber"])
    ]
)
data class ShippingLabelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long? = null,
    val invoiceNumber: String = "",
    val recipientName: String,
    val recipientPhone: String = "",
    val recipientAddress: String,
    val recipientCity: String = "",
    val recipientPostalCode: String = "",
    val courier: String = "",
    val serviceType: String = "",
    val trackingNumber: String = "",
    val shippingCost: Double = 0.0,
    val weightGram: Int = 0,
    val status: String = "draft",   // draft, printed, shipped, delivered, cancelled
    val notes: String = "",
    val printedAt: String? = null,
    val shippedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
