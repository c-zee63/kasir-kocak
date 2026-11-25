package com.kasirtokoq.app.data.local.dao

import androidx.room.*
import com.kasirtokoq.app.data.local.entities.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getByProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC LIMIT 100")
    fun getRecent(): Flow<List<StockMovementEntity>>

    @Insert
    suspend fun insert(movement: StockMovementEntity): Long
}
