package com.kasirtokoq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kasirtokoq.app.ui.screens.*
import com.kasirtokoq.app.ui.theme.KasirTokoQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KasirTokoQTheme {
                KasirTokoQAppScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    object Products : Screen("products", "Produk", Icons.Default.Inventory)
    object Reports : Screen("reports", "Laporan", Icons.Default.BarChart)
    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

@Composable
fun KasirTokoQAppScreen() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Pos, Screen.Products, Screen.Reports, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Pos.route) { PosScreen() }
            composable(Screen.Products.route) { ProductsScreen() }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
