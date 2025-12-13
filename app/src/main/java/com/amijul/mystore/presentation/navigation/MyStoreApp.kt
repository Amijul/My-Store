package com.amijul.mystore.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.amijul.mystore.presentation.navigation.component.BottomNavBar
import com.amijul.mystore.ui.account.AccountScreen
import com.amijul.mystore.ui.home.HomeScreen
import com.amijul.mystore.ui.home.HomeViewModel
import com.amijul.mystore.ui.order.OrderScreen
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.domain.account.AccountNavAction
import com.amijul.mystore.domain.account.AccountUi
import com.amijul.mystore.domain.navigation.Routes

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
                    modifier = Modifier
                        .background(Color(0xFFFFBFC0))
                        .padding(innerPadding),
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
                    modifier = Modifier.padding(innerPadding),
                    accountUi = AccountUi(
                        name = "Amijul",
                        email = "email",
                        photoUrl = ""
                    ),
                    onItemClick = { action ->
                        when (action) {
                            AccountNavAction.Orders -> {
                                // If your Orders is tab index 1, just switch tab:
                                homeViewModel.setBottomNav(1) // implement if not exists
                            }
                            AccountNavAction.MyDetails -> navController.navigate(Routes.MyDetails.route)
                            AccountNavAction.DeliveryAddress -> navController.navigate(Routes.DeliveryAddress.route)
                            AccountNavAction.Help -> navController.navigate(Routes.Help.route)
                            AccountNavAction.About -> navController.navigate(Routes.About.route)
                        }
                    },
                    onLogout = {
                        navController.navigate(Routes.Login.route) { popUpTo(0) }
                    }
                )
            }

        }
    }
}
