package com.amijul.mystore.domain.navigation

import android.net.Uri

sealed class Routes(val route: String) {

    data object AuthGate : Routes("authGate")
    data object SignIn : Routes("signIn")
    data object SignUp : Routes("signup")
    data object Main : Routes("main")

    // Existing tabs
    data object Account : Routes("account")
    data object Orders : Routes("orders")

    // Store flow routes (you already use these patterns in RootNavHost as raw strings)
    data object Products : Routes("products/{storeId}/{storeName}") {
        fun createRoute(storeId: String, storeName: String): String =
            "products/$storeId/${Uri.encode(storeName)}"
    }

    data object ProductDetail : Routes("productDetail/{storeId}/{storeName}") {
        fun createRoute(storeId: String, storeName: String): String =
            "productDetail/$storeId/${Uri.encode(storeName)}"
    }

    // UPDATED: make Cart store-scoped (required for clean architecture)
    data object Cart : Routes("cart/{storeId}/{storeName}") {
        fun createRoute(storeId: String, storeName: String): String =
            "cart/$storeId/${Uri.encode(storeName)}"
    }

    // NEW: Checkout route (COD today, later other payment modes)
    data object Checkout : Routes("checkout/{storeId}/{storeName}") {
        fun createRoute(storeId: String, storeName: String): String =
            "checkout/$storeId/${Uri.encode(storeName)}"
    }

    data object OrderDetails: Routes("order_details/{storeId}/{orderId}") {
        fun createRoute(storeId: String, orderId: String) = "order_details/$storeId/$orderId"
    }

    // Account stack
    data object MyDetails : Routes("myDetails")
    data object Addresses : Routes("addresses")
    data object EditAddress : Routes("editAddress")
    data object Help : Routes("help")
    data object About : Routes("about")

    data object SellerUpgrade : Routes("sellerUpgrade")

}
