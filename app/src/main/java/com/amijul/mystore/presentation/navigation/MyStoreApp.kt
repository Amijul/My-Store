package com.amijul.mystore.presentation.navigation

import android.net.Uri
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
import androidx.navigation.NavController
import com.amijul.mystore.presentation.navigation.component.BottomNavBar
import com.amijul.mystore.ui.account.AccountScreen
import com.amijul.mystore.ui.home.HomeScreen
import com.amijul.mystore.ui.home.HomeViewModel
import com.amijul.mystore.ui.order.OrderScreen
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreApp(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // read current tab from ViewModel
    val bottomNavIndex by homeViewModel.bottomNav.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyStore") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            BottomNavBar(homeViewModel = homeViewModel)
        }
    ) { innerPadding ->
        when (bottomNavIndex) {
            0 -> {
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onGoToProductList = { storeId, storeName ->
                        val encodedName = Uri.encode(storeName)
                        navController.navigate("products/$storeId/$encodedName")
                    }
                )
            }

            1 -> {
                OrderScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            2 -> {
                AccountScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
