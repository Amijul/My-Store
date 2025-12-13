package com.amijul.mystore.presentation.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amijul.mystore.domain.account.AccountNavAction
import com.amijul.mystore.domain.account.AccountUi
import com.amijul.mystore.domain.cart.CartItemUi
import com.amijul.mystore.domain.navigation.Routes
import com.amijul.mystore.ui.account.AccountScreen
import com.amijul.mystore.ui.cart.CartScreen
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.products.productdetails.ProductDetailScreen
import com.amijul.mystore.ui.products.ProductListScreen
import com.amijul.mystore.ui.products.ProductListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RootNavHost(
    cartViewModel: CartViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Main.route
    ) {
        // Main tabbed area (home / orders / account)
        composable(Routes.Main.route) {
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
                cartViewModel = cartViewModel,
                storeName = storeName,
                onOpenProductDetail = {
                    navController.navigate("productDetail/$storeId/${Uri.encode(storeName)}")
                }
            )

        }

        composable(
            route = "productDetail/{storeId}/{storeName}",
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType },
                navArgument("storeName") { type = NavType.StringType }
            )
        )
        { backStackEntry ->

            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("products/{storeId}/{storeName}")
            }

            val viewModel: ProductListViewModel = koinViewModel(
                viewModelStoreOwner = parentEntry,
                parameters = { parametersOf(storeId) }
            )

            ProductDetailScreen(
                viewModel = viewModel,
                cartViewModel = cartViewModel,
                storeName = storeName,
                onBack = {
                    viewModel.clearSelectedProduct()
                    navController.popBackStack()
                },

                onProceedToCheckout = {
                },
                onNavigation = {
                    navController.navigate(Routes.Cart.route)
                }
            )
        }

        composable(Routes.Cart.route){
            CartScreen(
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onProceedCheckout = {
                    // next: navigate to checkout route (later)
                }
            )
        }


        composable(Routes.MyDetails.route) { Text("My Details") }
        composable(Routes.DeliveryAddress.route) { Text("Delivery Address") }
        composable(Routes.Help.route) { Text("Help") }
        composable(Routes.About.route) { Text("About") }
        composable(Routes.Login.route) { Text("Login") }




    }
}





