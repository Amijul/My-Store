package com.amijul.mystore.domain.cart

// domain/cart/CartItemUi.kt
data class CartItemUi(
    val id: String,
    val name: String,
    val price: Float,
    val quantity: Int,
    val imageUrl: String,
    val size: String = ""
)