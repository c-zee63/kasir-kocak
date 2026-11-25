package com.kasirtokoq.app.data.local.dao

import androidx.room.*
import com.kasirtokoq.app.data.local.entities.SaleEntity
import com.kasirtokoq.app.data.local.entities.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date(createdAt) = date('now', 'localtime') AND isVoid = 0 ORDER BY createdAt DESC")
    fun getToday(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItems(saleId: Long): List<SaleItemEntity>

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertItems(items: List<SaleItemEntity>)

    @Transaction
    suspend fun insertSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insertSale(sale)
        insertItems(items.map { it.copy(saleId = saleId) })
        return saleId
    }

    @Query("UPDATE sales SET isVoid = 1, voidReason = :reason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun voidSale(id: Long, reason: String, updatedAt: String)

    @Query("""
        SELECT COALESCE(SUM(grandTotal), 0) FROM sales 
        WHERE date(createdAt) = date('now', 'localtime') AND isVoid = 0
    """)
    suspend fun getTodayRevenue(): Double

    @Query("""
        SELECT COALESCE(SUM(totalProfit), 0) FROM sales 
        WHERE date(createdAt) = date('now', 'localtime') AND isVoid = 0
    """)
    suspend fun getTodayProfit(): Double
}
