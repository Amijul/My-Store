package com.amijul.mystore.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amijul.mystore.ui.products.ProductListScreen
import com.amijul.mystore.ui.products.ProductListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RootNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main tabbed area (home / orders / account)
        composable("main") {
            MyStoreApp(navController = navController)
        }

        // Product page – full screen, no top/bottom from MyStoreApp
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
