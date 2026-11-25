package com.kasirtokoq.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kasirtokoq.app.data.local.entities.ProductEntity
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(private val repo: KasirRepository) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> = repo.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStock: StateFlow<List<ProductEntity>> = repo.getLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun addProduct(
        name: String,
        barcode: String?,
        price: Double,
        costPrice: Double,
        stock: Int,
        unit: String = "pcs"
    ) {
        viewModelScope.launch {
            val now = DateTimeUtils.now()
            val id = repo.saveProduct(
                ProductEntity(
                    name = name,
                    barcode = barcode?.ifBlank { null },
                    price = price,
                    costPrice = costPrice,
                    stock = stock,
                    unit = unit,
                    createdAt = now,
                    updatedAt = now
                )
            )
            _message.emit("Produk ditambahkan (ID: $id)")
        }
    }

    fun updateStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            val p = repo.getProductById(productId) ?: return@launch
            repo.updateProduct(p.copy(stock = newStock, updatedAt = DateTimeUtils.now()))
            _message.emit("Stok diperbarui")
        }
    }

    class Factory(private val repo: KasirRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(repo) as T
        }
    }
}
