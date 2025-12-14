package com.amijul.mystore.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amijul.mystore.domain.navigation.Routes
import com.amijul.mystore.presentation.auth.AuthViewModel
import com.amijul.mystore.presentation.auth.LoginScreen
import com.amijul.mystore.presentation.auth.SignUpScreen
import com.amijul.mystore.presentation.navigation.component.AuthGateLoading
import com.amijul.mystore.ui.account.address.EditAddressScreen
import com.amijul.mystore.ui.account.address.EditAddressViewModel
import com.amijul.mystore.ui.account.profile.EditUserProfileScreen
import com.amijul.mystore.ui.cart.CartScreen
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.products.ProductListScreen
import com.amijul.mystore.ui.products.ProductListViewModel
import com.amijul.mystore.ui.products.productdetails.ProductDetailScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RootNavHost(
    cartViewModel: CartViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Routes.AuthGate.route
    ) {

        composable(Routes.AuthGate.route) {

            val loggedIn by authViewModel.loggedIn.collectAsStateWithLifecycle()

            LaunchedEffect(loggedIn) {
                if (loggedIn) {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.AuthGate.route) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                } else {
                    navController.navigate(Routes.SignIn.route) {
                        popUpTo(Routes.AuthGate.route) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            }

            AuthGateLoading()

        }

        composable(Routes.SignIn.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onGoSignUp = { navController.navigate(Routes.SignUp.route) },
                onLoggedIn = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.AuthGate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onSignedUp = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.AuthGate.route) { inclusive = true }
                    }
                }
            )
        }


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


        composable(Routes.MyDetails.route) {
            EditUserProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Addresses.route) { Text("Addresses") }
        composable(Routes.Help.route) { Text("Help") }
        composable(Routes.About.route) { Text("About") }

        composable("editAddress") {
            EditAddressScreen(
                onBack = { navController.popBackStack() }
            )
        }





    }
}





