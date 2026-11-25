package com.kasirtokoq.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasirtokoq.app.KasirTokoQApp
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import com.kasirtokoq.app.viewmodel.PosViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PosScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as KasirTokoQApp
    val repo = remember { KasirRepository(app.database) }
    val vm: PosViewModel = viewModel(factory = PosViewModel.Factory(repo))

    val cart by vm.cart.collectAsState()
    val total by vm.cartTotal.collectAsState()
    val searchResults by vm.searchResults.collectAsState()
    val query by vm.searchQuery.collectAsState()

    var showCheckout by remember { mutableStateOf(false) }
    var amountPaid by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("cash") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.message.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Kasir / POS", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { vm.onSearchChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari nama / barcode...") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotBlank()) vm.addByBarcode(query)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan / Cari")
                    }
                },
                singleLine = true
            )

            if (searchResults.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(searchResults) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = {
                                    Text("${DateTimeUtils.formatCurrency(product.price)} • Stok: ${product.stock}")
                                },
                                modifier = Modifier.clickable { vm.addToCart(product) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Keranjang (${cart.size} item)", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart, key = { it.productId ?: it.name }) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${item.qty} x ${DateTimeUtils.formatCurrency(item.price)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { vm.updateQty(item.productId, item.qty - 1) }) {
                                Icon(Icons.Default.Remove, null)
                            }
                            Text("${item.qty}", modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = { vm.updateQty(item.productId, item.qty + 1) }) {
                                Icon(Icons.Default.Add, null)
                            }
                            IconButton(onClick = { vm.removeFromCart(item.productId) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            DateTimeUtils.formatCurrency(total),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showCheckout = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("Bayar")
                    }
                }
            }
        }
    }

    if (showCheckout) {
        AlertDialog(
            onDismissRequest = { showCheckout = false },
            title = { Text("Pembayaran") },
            text = {
                Column {
                    Text("Total: ${DateTimeUtils.formatCurrency(total)}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountPaid,
                        onValueChange = { amountPaid = it },
                        label = { Text("Jumlah Bayar") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        FilterChip(
                            selected = paymentMethod == "cash",
                            onClick = { paymentMethod = "cash" },
                            label = { Text("Tunai") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = paymentMethod == "transfer",
                            onClick = { paymentMethod = "transfer" },
                            label = { Text("Transfer") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = paymentMethod == "qris",
                            onClick = { paymentMethod = "qris" },
                            label = { Text("QRIS") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val paid = amountPaid.toDoubleOrNull() ?: total
                    vm.checkout(paymentMethod, paid)
                    showCheckout = false
                    amountPaid = ""
                }) {
                    Text("Proses")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckout = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
