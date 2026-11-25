package com.kasirtokoq.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kasirtokoq.app.data.local.entities.SettingsEntity
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: KasirRepository) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = repo.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun save(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        footnote: String
    ) {
        viewModelScope.launch {
            val current = repo.getSettingsOnce()
            val now = DateTimeUtils.now()
            val updated = (current ?: SettingsEntity(id = 1)).copy(
                storeName = storeName,
                storeAddress = storeAddress,
                storePhone = storePhone,
                receiptFootnote = footnote,
                updatedAt = now,
                createdAt = current?.createdAt ?: now
            )
            repo.saveSettings(updated)
            _message.emit("Pengaturan disimpan")
        }
    }

    class Factory(private val repo: KasirRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repo) as T
        }
    }
}
