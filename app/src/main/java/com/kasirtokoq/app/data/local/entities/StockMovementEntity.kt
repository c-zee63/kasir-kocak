package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["type"]),
        Index(value = ["createdAt"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: String,               // in, out, sale, adjust, return, void
    val qty: Int,                   // positive = masuk, negative = keluar
    val qtyBefore: Int,
    val qtyAfter: Int,
    val referenceType: String = "",
    val referenceId: Long? = null,
    val notes: String = "",
    val createdBy: String = "system",
    val createdAt: String = ""
)
