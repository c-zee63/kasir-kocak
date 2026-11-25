package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["name"]),
        Index(value = ["categoryId"]),
        Index(value = ["isActive"]),
        Index(value = ["stock"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long? = null,
    val name: String,
    val barcode: String? = null,
    val sku: String? = null,
    val description: String = "",
    val price: Double,              // harga jual
    val costPrice: Double = 0.0,    // harga modal
    val stock: Int = 0,
    val minStock: Int = 0,
    val unit: String = "pcs",
    val photoPath: String = "",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
