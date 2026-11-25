package com.kasirtokoq.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kasirtokoq.app.data.local.entities.ProductEntity
import com.kasirtokoq.app.data.repository.CartItem
import com.kasirtokoq.app.data.repository.KasirRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PosViewModel(private val repo: KasirRepository) : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<ProductEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else repo.searchProducts(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Double> = _cart.map { items -> items.sumOf { it.subtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartProfit: StateFlow<Double> = _cart.map { items -> items.sumOf { it.profit } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _lastSaleId = MutableStateFlow<Long?>(null)
    val lastSaleId = _lastSaleId.asStateFlow()

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: ProductEntity, qty: Int = 1) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.productId == product.id }
        if (existing >= 0) {
            val old = current[existing]
            current[existing] = old.copy(qty = old.qty + qty)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    barcode = product.barcode ?: "",
                    price = product.price,
                    costPrice = product.costPrice,
                    qty = qty
                )
            )
        }
        _cart.value = current
    }

    fun addByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = repo.getProductByBarcode(barcode)
            if (product != null) {
                addToCart(product)
                _message.emit("Ditambahkan: ${product.name}")
            } else {
                _message.emit("Produk tidak ditemukan: $barcode")
            }
        }
    }

    fun updateQty(productId: Long?, qty: Int) {
        if (qty <= 0) {
            removeFromCart(productId)
            return
        }
        _cart.value = _cart.value.map {
            if (it.productId == productId) it.copy(qty = qty) else it
        }
    }

    fun removeFromCart(productId: Long?) {
        _cart.value = _cart.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun checkout(paymentMethod: String, amountPaid: Double, customerName: String = "") {
        viewModelScope.launch {
            if (_cart.value.isEmpty()) {
                _message.emit("Keranjang masih kosong")
                return@launch
            }
            val result = repo.processSale(
                items = _cart.value,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                customerName = customerName
            )
            result.onSuccess { saleId ->
                _lastSaleId.value = saleId
                clearCart()
                _message.emit("Transaksi berhasil! ID: $saleId")
            }.onFailure {
                _message.emit("Gagal: ${it.message}")
            }
        }
    }

    class Factory(private val repo: KasirRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PosViewModel(repo) as T
        }
    }
}
