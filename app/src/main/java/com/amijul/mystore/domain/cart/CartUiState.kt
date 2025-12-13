package com.amijul.mystore.domain.cart

// domain/cart/CartUiState.kt
data class CartUiState(
    val items: List<CartItemUi> = emptyList(),
    val subTotal: Float = 0F,
    val shipping: Float = 40F,
    val total: Float = 0F
)