package com.amijul.mystore.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amijul.mystore.presentation.navigation.component.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreApp() {

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe current route to decide visible chrome
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Product screen routes start with "products"
    val isProductScreen = currentRoute?.startsWith("products") == true

    Scaffold(
        topBar = {
            if (!isProductScreen) {
                TopAppBar(
                    title = { Text("MyStore") }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (!isProductScreen) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        MyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
