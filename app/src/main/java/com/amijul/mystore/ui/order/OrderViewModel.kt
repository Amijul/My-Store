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
    private val orderRepo: OrderLocalRepository,
    private val orderRemoteRepo: OrderRemoteRepository,
    private val buyerOrdersRepo: BuyerOrdersFirestoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderUiState())
    val state: StateFlow<OrderUiState> = _state.asStateFlow()

    private var activeJob: Job? = null
    private var pastJob: Job? = null
    private var addressJob: Job? = null

    private var started = false

    fun start() {
        if (started) return
        started = true

        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.value = OrderUiState(
                isLoading = false,
                message = "User is not logged in"
            )
            return
        }

        activeJob?.cancel()
        pastJob?.cancel()
        addressJob?.cancel()

        _state.value = _state.value.copy(isLoading = true, message = null)

        val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        addressJob = viewModelScope.launch {
            addressDao.observeDefault(uid).collectLatest { addr ->
                val addressText = if (addr == null) "" else buildString {
                    append(addr.fullName)
                    if (addr.phone.isNotBlank()) append(" • ${addr.phone}")
                    append("\n${addr.line1}")
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
            try {
                buyerOrdersRepo.observeActive(uid).collectLatest { list ->
                    val activeUi = list.map { o ->
                        ActiveOrderCardUi(
                            orderId = o.orderId,
                            status = o.status,
                            dateText = if (o.createdAtMillis > 0) {
                                df.format(Date(o.createdAtMillis))
                            } else {
                                ""
                            },
                            itemCount = 0,
                            total = o.grandTotal.toFloat()
                        )
                    }

                    val cur = _state.value
                    _state.value = cur.copy(
                        isLoading = false,
                        active = cur.active.copy(orders = activeUi),
                        message = null
                    )
                }
            } catch (e: Exception) {
                // Do not clear existing list; just show message
                val cur = _state.value
                _state.value = cur.copy(
                    isLoading = false,
                    message = e.message ?: "Failed to load active orders"
                )
            }
        }

        pastJob = viewModelScope.launch {
            try {
                buyerOrdersRepo.observePast(uid).collectLatest { list ->
                    val pastUi = list.map { o ->
                        PastOrderUi(
                            orderId = o.orderId,
                            status = o.status,
                            dateText = if (o.createdAtMillis > 0) {
                                df.format(Date(o.createdAtMillis))
                            } else {
                                ""
                            },
                            itemCount = 0,
                            total = o.grandTotal.toFloat()
                        )
                    }

                    val cur = _state.value
                    _state.value = cur.copy(
                        isLoading = false,
                        past = pastUi,
                        message = null
                    )
                }
            } catch (e: Exception) {
                val cur = _state.value
                _state.value = cur.copy(
                    isLoading = false,
                    message = e.message ?: "Failed to load past orders"
                )
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
            _state.value = current.copy(
                message = "Add a delivery address first."
            )
            return
        }

        viewModelScope.launch {
            try {
                val cart = cartRepo.getCartOnce(uid, storeId)
                if (cart.isEmpty()) {
                    _state.value = _state.value.copy(
                        message = "Cart is empty."
                    )
                    return@launch
                }

                val addr = addressDao.getDefault(uid)
                if (addr == null) {
                    _state.value = _state.value.copy(
                        message = "No default address found."
                    )
                    return@launch
                }

                // 🔹 Calculate totals locally
                val itemsTotal = cart.sumOf { it.unitPrice * it.quantity.toDouble() }

                val shipping = 0.0
                val grandTotal = itemsTotal + shipping

                // 🔹 CALL FIXED createOrder()
                val orderId = orderRemoteRepo.createOrder(
                    storeId = storeId,
                    storeName = storeName,
                    buyerName = addr.fullName,
                    buyerPhone = addr.phone,
                    itemsTotal = itemsTotal,
                    shipping = shipping,
                    grandTotal = grandTotal
                )

                cartRepo.clearStore(uid, storeId)

                _state.value = _state.value.copy(
                    message = "Order placed. OrderId: $orderId"
                )
                onSuccess(orderId)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "Order failed"
                )
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
