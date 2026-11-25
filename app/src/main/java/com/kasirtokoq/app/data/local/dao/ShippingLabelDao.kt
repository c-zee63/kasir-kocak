package com.kasirtokoq.app.data.local.dao

import androidx.room.*
import com.kasirtokoq.app.data.local.entities.ShippingLabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShippingLabelDao {
    @Query("SELECT * FROM shipping_labels ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ShippingLabelEntity>>

    @Query("SELECT * FROM shipping_labels WHERE id = :id")
    suspend fun getById(id: Long): ShippingLabelEntity?

    @Insert
    suspend fun insert(label: ShippingLabelEntity): Long

    @Update
    suspend fun update(label: ShippingLabelEntity)

    @Query("UPDATE shipping_labels SET status = :status, printedAt = :printedAt WHERE id = :id")
    suspend fun markPrinted(id: Long, status: String = "printed", printedAt: String)
}
