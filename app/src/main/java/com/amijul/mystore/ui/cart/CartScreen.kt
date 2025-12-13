package com.amijul.mystore.ui.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.cart.components.Carts

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onProceedCheckout: () -> Unit
) {
    val state by cartViewModel.state.collectAsStateWithLifecycle()

    Carts(
        state = state,
        onBack = onBack,
        onIncreaseQty = cartViewModel::increase,
        onDecreaseQty = cartViewModel::decrease,
        onRemoveItem = cartViewModel::remove,
        onProceedCheckout = onProceedCheckout
    )
}
