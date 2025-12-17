package com.amijul.mystore.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.domain.cart.CartItemUi
import com.amijul.mystore.domain.cart.CartLocalRepository
import com.amijul.mystore.domain.cart.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CartViewModel(
    private val userIdProvider: () -> String?,
    private val cartRepo: CartLocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    fun start() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.value = CartUiState(items = emptyList(), subTotal = 0f, shipping = 0f, total = 0f)
            return
        }

        viewModelScope.launch {
            cartRepo.observeCart(uid)
                .map { entities ->
                    entities.map {
                        CartItemUi(
                            id = it.productId,
                            name = it.name,
                            price = it.unitPrice,
                            quantity = it.quantity,
                            imageUrl = it.imageUrl,
                            size = ""
                        )
                    }
                }
                .collectLatest { uiItems ->
                    _state.value = recalc(uiItems)
                }
        }
    }

    fun addToCart(item: CartItemUi, qty: Int) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        viewModelScope.launch {
            cartRepo.addOrIncrease(
                userId = uid,
                productId = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                unitPrice = item.price,
                addQty = qty
            )
        }
    }

    fun increase(productId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return
        val current = _state.value.items.firstOrNull { it.id == productId } ?: return

        viewModelScope.launch {
            cartRepo.setQty(uid, productId, current.quantity + 1)
        }
    }

    fun decrease(productId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return
        val current = _state.value.items.firstOrNull { it.id == productId } ?: return
        val newQty = (current.quantity - 1).coerceAtLeast(1)

        viewModelScope.launch {
            cartRepo.setQty(uid, productId, newQty)
        }
    }

    fun remove(productId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        viewModelScope.launch {
            cartRepo.remove(uid, productId)
        }
    }

    fun clearCart() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        viewModelScope.launch {
            cartRepo.clear(uid)
        }
    }

    private fun recalc(items: List<CartItemUi>): CartUiState {
        val subTotal = items.sumOf { (it.price * it.quantity).toDouble() }.toFloat()
        val shipping = if (items.isEmpty()) 0f else 40f
        val total = subTotal + shipping
        return CartUiState(items = items, subTotal = subTotal, shipping = shipping, total = total)
    }
}
