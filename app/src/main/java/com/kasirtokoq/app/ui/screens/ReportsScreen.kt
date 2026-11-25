package com.kasirtokoq.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kasirtokoq.app.KasirTokoQApp
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import com.kasirtokoq.app.utils.ExcelExporter
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as KasirTokoQApp
    val repo = remember { KasirRepository(app.database) }
    val scope = rememberCoroutineScope()

    val sales by repo.getSales().collectAsState(initial = emptyList())
    val lowStock by repo.getLowStock().collectAsState(initial = emptyList())
    val products by repo.getActiveProducts().collectAsState(initial = emptyList())

    var message by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Laporan", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    val file = ExcelExporter.exportProductsToCsv(context, products)
                    message = if (file != null) "Export berhasil: ${file.name}" else "Gagal export"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Produk ke CSV/Excel")
        }

        Spacer(Modifier.height(16.dp))
        Text("Stok Menipis (${lowStock.size})", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
            items(lowStock) { p ->
                Text("• ${p.name} — sisa ${p.stock}")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Transaksi Terakhir", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(sales.take(20)) { s ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(s.invoiceNumber, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DateTimeUtils.formatCurrency(s.grandTotal)} • Profit ${DateTimeUtils.formatCurrency(s.totalProfit)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(s.createdAt, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
