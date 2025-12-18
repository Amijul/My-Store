package com.amijul.mystore.ui.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.cart.components.Carts
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CartScreen(
    storeId: String,
    storeName: String,
    onBack: () -> Unit,
    onProceedCheckout: () -> Unit
) {
    val cartViewModel: CartViewModel = koinViewModel(
        parameters = { parametersOf(storeId, storeName) }
    )

    LaunchedEffect(storeId) { cartViewModel.start() }

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
