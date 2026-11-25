package com.kasirtokoq.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kasirtokoq.app.KasirTokoQApp
import com.kasirtokoq.app.data.repository.KasirRepository
import com.kasirtokoq.app.utils.DateTimeUtils
import com.kasirtokoq.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as KasirTokoQApp
    val repo = remember { KasirRepository(app.database) }
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repo))

    val revenue by vm.revenue.collectAsState()
    val profit by vm.profit.collectAsState()
    val lowStock by vm.lowStockCount.collectAsState()
    val todayCount by vm.todaySalesCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KasirTokoQ",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Sistem Kasir Offline untuk Toko & UMKM",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Omzet Hari Ini", DateTimeUtils.formatCurrency(revenue), Modifier.weight(1f))
            SummaryCard("Profit Hari Ini", DateTimeUtils.formatCurrency(profit), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Transaksi", todayCount.toString(), Modifier.weight(1f))
            SummaryCard("Stok Menipis", lowStock.toString(), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = { vm.refresh() }) {
            Text("Refresh Data")
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
