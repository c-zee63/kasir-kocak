package com.kasirtokoq.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasirtokoq.app.KasirTokoQApp
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import com.kasirtokoq.app.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProductsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as KasirTokoQApp
    val repo = remember { KasirRepository(app.database) }
    val vm: ProductViewModel = viewModel(factory = ProductViewModel.Factory(repo))

    val products by vm.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.message.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Daftar Produk", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            if (products.isEmpty()) {
                Text(
                    "Belum ada produk. Tekan tombol + untuk menambah.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(products, key = { it.id }) { p ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Barcode: ${p.barcode ?: "-"} | Stok: ${p.stock} ${p.unit}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Jual: ${DateTimeUtils.formatCurrency(p.price)} | Modal: ${DateTimeUtils.formatCurrency(p.costPrice)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Produk") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, singleLine = true)
                    OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode (opsional)") }, singleLine = true)
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga Jual") }, singleLine = true)
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Harga Modal") }, singleLine = true)
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok Awal") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val c = cost.toDoubleOrNull() ?: 0.0
                    val s = stock.toIntOrNull() ?: 0
                    if (name.isNotBlank() && p > 0) {
                        vm.addProduct(name, barcode.ifBlank { null }, p, c, s)
                        name = ""; barcode = ""; price = ""; cost = ""; stock = "0"
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}
