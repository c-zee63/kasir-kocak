package com.kasirtokoq.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasirtokoq.app.KasirTokoQApp
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as KasirTokoQApp
    val repo = remember { KasirRepository(app.database) }
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repo))

    val settings by vm.settings.collectAsState()

    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var footnote by remember { mutableStateOf("") }

    LaunchedEffect(settings) {
        settings?.let {
            storeName = it.storeName
            storeAddress = it.storeAddress
            storePhone = it.storePhone
            footnote = it.receiptFootnote
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.message.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Pengaturan Toko", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Nama Toko") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = storeAddress,
                onValueChange = { storeAddress = it },
                label = { Text("Alamat Toko") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = storePhone,
                onValueChange = { storePhone = it },
                label = { Text("Telepon") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = footnote,
                onValueChange = { footnote = it },
                label = { Text("Footnote Struk") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.save(storeName, storeAddress, storePhone, footnote) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Pengaturan")
            }

            Spacer(Modifier.height(32.dp))
            Text("Printer Thermal Bluetooth", style = MaterialTheme.typography.titleMedium)
            Text(
                "Gunakan class ThermalPrinterHelper untuk mencetak struk ESC/POS.\nHubungkan printer yang sudah di-pair di pengaturan Bluetooth HP.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
