package com.kasirtokoq.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kasirtokoq.app.data.local.dao.*
import com.kasirtokoq.app.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        SettingsEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        ShippingLabelEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun categoryDao(): CategoryDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun shippingLabelDao(): ShippingLabelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kasirtokoq.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default settings
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val now = LocalDateTime.now().format(formatter)
                                    database.settingsDao().upsert(
                                        SettingsEntity(
                                            id = 1,
                                            storeName = "KasirTokoQ",
                                            storeAddress = "Jl. Contoh No. 123",
                                            storePhone = "0812-3456-7890",
                                            receiptFootnote = "Terima kasih telah berbelanja!\nFollow IG: @kasirtokoq",
                                            createdAt = now,
                                            updatedAt = now
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
