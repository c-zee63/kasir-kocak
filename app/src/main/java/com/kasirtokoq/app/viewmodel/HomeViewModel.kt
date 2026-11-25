package com.kasirtokoq.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kasirtokoq.app.data.repository.KasirRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: KasirRepository) : ViewModel() {

    private val _revenue = MutableStateFlow(0.0)
    val revenue = _revenue.asStateFlow()

    private val _profit = MutableStateFlow(0.0)
    val profit = _profit.asStateFlow()

    val lowStockCount = repo.getLowStock()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todaySalesCount = repo.getTodaySales()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _revenue.value = repo.getTodayRevenue()
            _profit.value = repo.getTodayProfit()
        }
    }

    class Factory(private val repo: KasirRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repo) as T
        }
    }
}
