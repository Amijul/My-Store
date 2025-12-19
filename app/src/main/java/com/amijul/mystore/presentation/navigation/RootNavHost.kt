package com.amijul.mystore.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.amijul.mystore.ui.account.profile.EditUserProfileScreen
import com.amijul.mystore.ui.cart.CartScreen
import com.amijul.mystore.ui.products.ProductListScreen
import com.amijul.mystore.ui.products.ProductListViewModel
import com.amijul.mystore.ui.products.productdetails.ProductDetailScreen
import com.amijul.mystore.ui.seller.SellerUpgradeScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RootNavHost(
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

        // Products
        composable(
            route = Routes.Products.route,
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
                storeId = storeId,
                viewModel = viewModel,
                storeName = storeName,
                onOpenProductDetail = {
                    navController.navigate(Routes.ProductDetail.createRoute(storeId, storeName))
                }
            )
        }

        // Product detail (shares ProductListViewModel using parent entry)
        composable(
            route = Routes.ProductDetail.route,
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType },
                navArgument("storeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.Products.route)
            }

            val viewModel: ProductListViewModel = koinViewModel(
                viewModelStoreOwner = parentEntry,
                parameters = { parametersOf(storeId) }
            )

            ProductDetailScreen(
                storeId = storeId,
                viewModel = viewModel,
                storeName = storeName,
                onBack = {
                    viewModel.clearSelectedProduct()
                    navController.popBackStack()
                },
                onProceedToCheckout = {
                    navController.navigate(Routes.Checkout.createRoute(storeId, storeName))
                },
                onNavigation = {
                    navController.navigate(Routes.Cart.createRoute(storeId, storeName))
                }
            )
        }

        // Cart (NOW store scoped)
        composable(
            route = Routes.Cart.route,
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType },
                navArgument("storeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""

            CartScreen(
                storeId = storeId,
                storeName = storeName,
                onBack = { navController.popBackStack() },
                onProceedCheckout = { navController.navigate(Routes.Checkout.createRoute(storeId, storeName)) }
            )

        }

        composable(
            route = Routes.Checkout.route,
            arguments = listOf(
                navArgument("storeId") { type = NavType.StringType },
                navArgument("storeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""

            com.amijul.mystore.ui.checkout.CheckoutScreen(
                storeId = storeId,
                storeName = storeName,
                onBack = { navController.popBackStack() }
            )
        }


        // Account stack
        composable(Routes.MyDetails.route) {
            EditUserProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Addresses.route) { Text("Addresses") }
        composable(Routes.Help.route) { Text("Help") }
        composable(Routes.About.route) { Text("About") }

        composable(Routes.EditAddress.route) {
            EditAddressScreen(
                onBack = { navController.popBackStack() }
            )
        }



        composable(Routes.SellerUpgrade.route) {
            SellerUpgradeScreen(
                onDone = { navController.popBackStack() }
            )
        }

    }
}
