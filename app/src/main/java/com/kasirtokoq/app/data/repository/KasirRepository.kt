package com.kasirtokoq.app.data.repository

import com.kasirtokoq.app.data.local.AppDatabase
import com.kasirtokoq.app.data.local.entities.*
import com.kasirtokoq.app.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class KasirRepository(private val db: AppDatabase) {

    // ========== Settings ==========
    fun getSettings(): Flow<SettingsEntity?> = db.settingsDao().get()
    suspend fun getSettingsOnce(): SettingsEntity? = db.settingsDao().getOnce()
    suspend fun saveSettings(settings: SettingsEntity) = db.settingsDao().upsert(settings)

    // ========== Products ==========
    fun getActiveProducts(): Flow<List<ProductEntity>> = db.productDao().getAllActive()
    fun searchProducts(query: String): Flow<List<ProductEntity>> = db.productDao().search(query)
    fun getLowStock(): Flow<List<ProductEntity>> = db.productDao().getLowStock()
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = db.productDao().getByBarcode(barcode)
    suspend fun getProductById(id: Long): ProductEntity? = db.productDao().getById(id)
    suspend fun saveProduct(product: ProductEntity): Long = db.productDao().insert(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().update(product)

    // ========== Categories ==========
    fun getCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getAllActive()
    suspend fun saveCategory(category: CategoryEntity): Long = db.categoryDao().insert(category)

    // ========== Sales ==========
    fun getSales(): Flow<List<SaleEntity>> = db.saleDao().getAll()
    fun getTodaySales(): Flow<List<SaleEntity>> = db.saleDao().getToday()
    suspend fun getTodayRevenue(): Double = db.saleDao().getTodayRevenue()
    suspend fun getTodayProfit(): Double = db.saleDao().getTodayProfit()

    /**
     * Proses transaksi lengkap:
     * 1. Insert sale header
     * 2. Insert sale items
     * 3. Kurangi stok produk
     * 4. Catat stock movement
     */
    suspend fun processSale(
        items: List<CartItem>,
        paymentMethod: String,
        amountPaid: Double,
        customerName: String = "",
        notes: String = "",
        cashierName: String = "Kasir"
    ): Result<Long> {
        return try {
            val settings = getSettingsOnce()
            val prefix = settings?.receiptPrefix ?: "KTQ"
            val invoice = DateTimeUtils.generateInvoiceNumber(prefix)
            val now = DateTimeUtils.now()

            val totalAmount = items.sumOf { it.subtotal }
            val totalProfit = items.sumOf { it.profit }
            val change = amountPaid - totalAmount

            val sale = SaleEntity(
                invoiceNumber = invoice,
                totalAmount = totalAmount,
                discount = 0.0,
                taxAmount = 0.0,
                grandTotal = totalAmount,
                totalProfit = totalProfit,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                changeAmount = if (change > 0) change else 0.0,
                customerName = customerName,
                notes = notes,
                cashierName = cashierName,
                createdAt = now,
                updatedAt = now
            )

            val saleId = db.saleDao().insertSale(sale)

            val saleItems = items.map { item ->
                SaleItemEntity(
                    saleId = saleId,
                    productId = item.productId,
                    productName = item.name,
                    barcode = item.barcode,
                    qty = item.qty,
                    unitPrice = item.price,
                    unitCost = item.costPrice,
                    subtotal = item.subtotal,
                    profit = item.profit,
                    createdAt = now
                )
            }
            db.saleDao().insertItems(saleItems)

            // Update stock + movement
            items.forEach { item ->
                if (item.productId != null) {
                    val product = db.productDao().getById(item.productId)
                    if (product != null) {
                        val before = product.stock
                        val after = before - item.qty
                        db.productDao().adjustStock(item.productId, -item.qty, now)
                        db.stockMovementDao().insert(
                            StockMovementEntity(
                                productId = item.productId,
                                type = "sale",
                                qty = -item.qty,
                                qtyBefore = before,
                                qtyAfter = after,
                                referenceType = "sale",
                                referenceId = saleId,
                                notes = "Penjualan $invoice",
                                createdBy = cashierName,
                                createdAt = now
                            )
                        )
                    }
                }
            }

            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun voidSale(saleId: Long, reason: String) {
        val now = DateTimeUtils.now()
        db.saleDao().voidSale(saleId, reason, now)
        // Note: stock return logic bisa ditambahkan di trigger atau di sini
    }

    // ========== Shipping ==========
    fun getShippingLabels(): Flow<List<ShippingLabelEntity>> = db.shippingLabelDao().getAll()
    suspend fun saveShippingLabel(label: ShippingLabelEntity): Long = db.shippingLabelDao().insert(label)
}

/** Model sederhana untuk item di keranjang */
data class CartItem(
    val productId: Long?,
    val name: String,
    val barcode: String = "",
    val price: Double,
    val costPrice: Double,
    val qty: Int
) {
    val subtotal: Double get() = price * qty
    val profit: Double get() = (price - costPrice) * qty
}
