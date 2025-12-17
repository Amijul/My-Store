package com.amijul.mystore.ui.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.cart.components.Carts
import org.koin.androidx.compose.koinViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel = koinViewModel(),
    onBack: () -> Unit,
    onProceedCheckout: () -> Unit
) {
    LaunchedEffect(Unit) { cartViewModel.start() }

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
