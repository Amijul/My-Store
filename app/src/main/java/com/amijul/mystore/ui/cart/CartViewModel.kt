package com.amijul.mystore.ui.cart

import androidx.lifecycle.ViewModel
import com.amijul.mystore.domain.cart.CartItemUi
import com.amijul.mystore.domain.cart.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel: ViewModel() {

    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    fun addToCart(item: CartItemUi, qty: Int) {
        _state.update { current ->
            val items = current.items.toMutableList()
            val index = items.indexOfFirst { it.id == item.id }

            if (index >= 0) {
                val old = items[index]
                items[index] = old.copy(quantity = old.quantity + qty)
            } else {
                items.add(item.copy(quantity = qty))
            }

            // return the new state
            recalc(items)
        }
    }


    fun increase(id: String) {
        _state.update { current->
            recalc(
                current.items.map {
                    if(it.id == id) it.copy(quantity = it.quantity + 1) else it
                }
            )
        }
    }

    fun decrease(id: String) {
        _state.update { current->
            recalc(
                current.items.map {
                    if(it.id == id && it.quantity > 1) {
                        it.copy(quantity = it.quantity - 1)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun remove(id: String) {
        _state.update { current->
            recalc( current.items.filterNot { it.id == id })
        }
    }

    private fun recalc(items: List<CartItemUi>): CartUiState {
        val subTotal = items.sumOf { (it.price * it.quantity).toDouble() }.toFloat()
        val shipping = if (items.isEmpty()) 0f else 40f
        val total = subTotal + shipping

        return CartUiState(
            items = items,
            subTotal = subTotal,
            shipping = shipping,
            total = total
        )
    }



}