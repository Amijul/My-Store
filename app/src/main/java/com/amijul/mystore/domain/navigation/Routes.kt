package com.amijul.mystore.domain.navigation

sealed class Routes(val route: String) {
    data object Login: Routes("login")
    data object Logout: Routes("logout")
    data object Main: Routes("main")
    data object Cart: Routes("cart")
    data object Account: Routes("account")
    data object Orders: Routes("orders")
    data object MyDetails: Routes("myDetails")
    data object DeliveryAddress: Routes("address")
    data object Help: Routes("help")
    data object About: Routes("about")

}