package com.kasirtokoq.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#3B82F6",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
