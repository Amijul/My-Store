package com.amijul.mystore.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.order.BuyerOrdersFirestoreRepository
import com.amijul.mystore.domain.order.OrderDocUi
import com.amijul.mystore.domain.order.OrderItemDocUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val loading: Boolean = true,
    val order: OrderDocUi? = null,
    val items: List<OrderItemDocUi> = emptyList(),
    val error: String? = null
)

class OrderDetailsViewModel(
    private val storeId: String,
    private val orderId: String,
    private val repo: BuyerOrdersFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderDetailsUiState())
    val state: StateFlow<OrderDetailsUiState> = _state.asStateFlow()

    fun start() {
        viewModelScope.launch {
            repo.observeOrder(storeId, orderId).collect { o ->
                _state.value = _state.value.copy(loading = false, order = o)
            }
        }
        viewModelScope.launch {
            repo.observeOrderItems(storeId, orderId).collect { list ->
                _state.value = _state.value.copy(items = list)
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            try {
                repo.cancelOrder(storeId, orderId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
