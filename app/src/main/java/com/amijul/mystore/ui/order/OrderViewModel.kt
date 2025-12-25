package com.amijul.mystore.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.order.BuyerOrdersFirestoreRepository
import com.amijul.mystore.domain.cart.CartLocalRepository
import com.amijul.mystore.domain.order.OrderLocalRepository
import com.amijul.mystore.domain.order.OrderRemoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveOrderCardUi(
    val orderId: String,
    val status: String,
    val dateText: String,
    val itemCount: Int,
    val total: Float
)

data class PastOrderUi(
    val orderId: String,
    val status: String,
    val dateText: String,
    val itemCount: Int,
    val total: Float
)

data class ActiveOrderUi(
    val addressText: String = "",
    val hasAddress: Boolean = false,
    val orders: List<ActiveOrderCardUi> = emptyList()
)

data class OrderUiState(
    val isLoading: Boolean = true,
    val active: ActiveOrderUi = ActiveOrderUi(),
    val past: List<PastOrderUi> = emptyList(),
    val message: String? = null
)

class OrderViewModel(
    private val userIdProvider: () -> String?,
    private val addressDao: AddressDao,
    private val cartRepo: CartLocalRepository,
    private val orderRepo: OrderLocalRepository, // keep as-is (even if unused for now)
    private val orderRemoteRepo: OrderRemoteRepository,
    private val buyerOrdersRepo: BuyerOrdersFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderUiState())
    val state: StateFlow<OrderUiState> = _state.asStateFlow()

    // IMPORTANT: prevents multiple collectors / listeners causing "disappear" flicker

    private var activeJob: Job? = null
    private var pastJob: Job? = null
    private var addressJob: Job? = null

    fun start() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.value = OrderUiState(isLoading = false, message = "User is not logged in")
            return
        }

        activeJob?.cancel()
        pastJob?.cancel()
        addressJob?.cancel()

        _state.value = _state.value.copy(isLoading = true, message = null)

        val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        addressJob = viewModelScope.launch {
            addressDao.observeDefault(uid).collect { addr ->
                val addressText = if (addr == null) "" else buildString {
                    append("${addr.fullName} • ${addr.phone}\n")
                    append(addr.line1)
                    if (!addr.line2.isNullOrBlank()) append(", ${addr.line2}")
                    append("\n${addr.city}, ${addr.state} - ${addr.pincode}")
                }

                val cur = _state.value
                _state.value = cur.copy(
                    isLoading = false,
                    active = cur.active.copy(
                        addressText = addressText,
                        hasAddress = addr != null
                    )
                )
            }
        }

        activeJob = viewModelScope.launch {
            buyerOrdersRepo.observeActive(uid)
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, message = e.message ?: "Failed to load active orders")
                }
                .collect { list ->
                    val activeUi = list.map { o ->
                        ActiveOrderCardUi(
                            orderId = o.orderId,
                            status = o.status,
                            dateText = if (o.createdAtMillis > 0) df.format(Date(o.createdAtMillis)) else "",
                            itemCount = 0,
                            total = o.grandTotal.toFloat()
                        )
                    }
                    val cur = _state.value
                    _state.value = cur.copy(
                        isLoading = false,
                        active = cur.active.copy(orders = activeUi)
                    )
                }
        }

        pastJob = viewModelScope.launch {
            buyerOrdersRepo.observePast(uid)
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, message = e.message ?: "Failed to load past orders")
                }
                .collect { list ->
                    val pastUi = list.map { o ->
                        PastOrderUi(
                            orderId = o.orderId,
                            status = o.status,
                            dateText = if (o.createdAtMillis > 0) df.format(Date(o.createdAtMillis)) else "",
                            itemCount = 0,
                            total = o.grandTotal.toFloat()
                        )
                    }
                    _state.value = _state.value.copy(isLoading = false, past = pastUi)
                }
        }
    }


    fun buyNow(
        storeId: String,
        storeName: String,
        onSuccess: (orderId: String) -> Unit
    ) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        val current = _state.value
        if (!current.active.hasAddress) {
            _state.value = current.copy(message = "Add a delivery address first.")
            return
        }

        viewModelScope.launch {
            try {
                val cart = cartRepo.getCartOnce(uid, storeId)
                if (cart.isEmpty()) {
                    _state.value = _state.value.copy(message = "Cart is empty.")
                    return@launch
                }

                val addr = addressDao.getDefault(uid)
                if (addr == null) {
                    _state.value = _state.value.copy(message = "No default address found.")
                    return@launch
                }

                val orderId = orderRemoteRepo.createOrder(
                    storeId = storeId,
                    storeName = storeName,
                    address = addr,
                    items = cart
                )

                cartRepo.clearStore(uid, storeId)
                _state.value = _state.value.copy(message = "Order placed. OrderId: $orderId")
                onSuccess(orderId)

            } catch (e: Exception) {
                _state.value = _state.value.copy(message = e.message ?: "Order failed")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    override fun onCleared() {
        activeJob?.cancel()
        pastJob?.cancel()
        addressJob?.cancel()
        super.onCleared()
    }
}
