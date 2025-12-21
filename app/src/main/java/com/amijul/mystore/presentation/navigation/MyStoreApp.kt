package com.amijul.mystore.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amijul.mystore.domain.account.AccountNavAction
import com.amijul.mystore.domain.navigation.Routes
import com.amijul.mystore.presentation.auth.AuthViewModel
import com.amijul.mystore.presentation.navigation.component.BottomNavBar
import com.amijul.mystore.ui.account.AccountScreen
import com.amijul.mystore.ui.account.AccountViewModel
import com.amijul.mystore.ui.home.HomeScreen
import com.amijul.mystore.ui.home.HomeViewModel
import com.amijul.mystore.ui.order.OrderScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreApp(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val bottomNavIndex by homeViewModel.bottomNav.collectAsStateWithLifecycle()
    val accountState by accountViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        accountViewModel.start()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { BottomNavBar(homeViewModel = homeViewModel) }
    ) { innerPadding ->

        when (bottomNavIndex) {

            // Home
            0 -> {
                HomeScreen(
                    modifier = Modifier
                        .background(Color(0xFFFFBFC0))
                        .padding(innerPadding),
                    onGoToProductList = { storeId, storeName ->
                        navController.navigate(
                            Routes.Products.createRoute(storeId = storeId, storeName = storeName)
                        )
                    }
                )
            }

            // Orders
            1 -> {
                OrderScreen()
            }

            // Account
            2 -> {
                AccountScreen(
                    modifier = Modifier.padding(innerPadding),
                    accountUi = accountState.accountUi,
                    onItemClick = { action ->
                        when (action) {
                            AccountNavAction.Cart -> {
                                navController.navigate(Routes.Cart.route)
                            }
                            AccountNavAction.MyDetails ->
                                navController.navigate(Routes.MyDetails.route)

                            AccountNavAction.DeliveryAddress ->
                                navController.navigate(Routes.EditAddress.route)

                            AccountNavAction.Help ->
                                navController.navigate(Routes.Help.route)

                            AccountNavAction.About ->
                                navController.navigate(Routes.About.route)

                        }
                    },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Routes.AuthGate.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
