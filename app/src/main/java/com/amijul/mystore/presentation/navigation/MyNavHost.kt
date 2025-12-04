package com.amijul.mystore.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.amijul.mystore.ui.account.AccountScreen
import com.amijul.mystore.ui.home.HomeScreen
import com.amijul.mystore.ui.order.OrderScreen
import com.amijul.mystore.ui.products.ProductListScreen
import com.amijul.mystore.ui.products.ProductListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onGoToProductList = { storeId, storeName ->
                    val encodedName = Uri.encode(storeName)
                    navController.navigate("products/$storeId/$encodedName")
                }
            )
        }

        composable("orders") {
            OrderScreen()
        }

        composable("account") {
            AccountScreen()
        }

        composable(
            route = "products/{storeId}/{storeName}",
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType },
                navArgument("storeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""

            val viewModel: ProductListViewModel = koinViewModel(
                parameters = { parametersOf(storeId) }
            )

            ProductListScreen(
                viewModel = viewModel,
                storeName = storeName
            )
        }
    }
}
