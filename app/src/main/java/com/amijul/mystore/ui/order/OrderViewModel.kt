package com.amijul.mystore.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.local.order.OrderEntity
import com.amijul.mystore.data.local.order.OrderItemEntity
import com.amijul.mystore.domain.cart.CartLocalRepository
import com.amijul.mystore.domain.order.OrderLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OrderItemUi(
    val storeId: String,
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Float,
    val quantity: Int
)
 {
    val lineTotal: Float get() = unitPrice * quantity
}

data class OrderPriceUi(
    val subTotal: Float = 0f,
    val shipping: Float = 0f,
    val total: Float = 0f
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
    val items: List<OrderItemUi> = emptyList(),
    val price: OrderPriceUi = OrderPriceUi()
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
    private val orderRepo: OrderLocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderUiState())
    val state: StateFlow<OrderUiState> = _state.asStateFlow()

    fun start() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.value = OrderUiState(isLoading = false, message = "User is not logged in")
            return
        }

        viewModelScope.launch {
            combine(
                addressDao.observeDefault(uid),
                orderRepo.observeOrders(uid)
            ) { addr, orders ->

                val addressText = if (addr == null) "" else buildString {
                    append("${addr.fullName} • ${addr.phone}\n")
                    append(addr.line1)
                    if (!addr.line2.isNullOrBlank()) append(", ${addr.line2}")
                    append("\n${addr.city}, ${addr.state} - ${addr.pincode}")
                }

                val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                val pastUi = orders.map { o ->
                    PastOrderUi(
                        orderId = o.orderId,
                        status = o.status,
                        dateText = df.format(Date(o.createdAtMillis)),
                        itemCount = 0, // optional: join items later
                        total = o.grandTotal
                    )
                }

                OrderUiState(
                    isLoading = false,
                    active = ActiveOrderUi(
                        addressText = addressText,
                        hasAddress = addr != null,
                        items = emptyList(), // Orders tab does not depend on cart
                        price = OrderPriceUi()
                    ),
                    past = pastUi,
                    message = null
                )
            }.collectLatest { newState ->
                _state.value = newState
            }
        }
    }

    /**
     * This should be called from Checkout flow (store-scoped), not Orders tab.
     * It creates local order snapshot and clears only the current store cart.
     */
    fun buyNow(storeId: String, storeName: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        val current = _state.value
        if (!current.active.hasAddress) {
            _state.value = current.copy(message = "Add a delivery address first.")
            return
        }

        viewModelScope.launch {
            val addr = addressDao.getDefault(uid) ?: run {
                _state.value = _state.value.copy(message = "No default address found.")
                return@launch
            }

            // NEW: one-shot cart snapshot
            val cart = cartRepo.getCartOnce(uid, storeId)
            if (cart.isEmpty()) {
                _state.value = _state.value.copy(message = "Cart is empty.")
                return@launch
            }

            val items = cart.map {
                OrderItemUi(
                    storeId = it.storeId,
                    productId = it.productId,
                    name = it.name,
                    imageUrl = it.imageUrl,
                    unitPrice = it.unitPrice,
                    quantity = it.quantity
                )
            }


            val subTotal = items.sumOf { (it.unitPrice * it.quantity).toDouble() }.toFloat()
            val shipping = if (items.isEmpty()) 0f else 40f
            val total = subTotal + shipping

            val orderId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()

            val order = OrderEntity(
                orderId = orderId,
                userId = uid,

                storeId = storeId,
                storeName = storeName,

                status = "PENDING",
                createdAtMillis = createdAt,
                itemsTotal = subTotal,
                shipping = shipping,
                grandTotal = total,
                fullName = addr.fullName,
                phone = addr.phone,
                line1 = addr.line1,
                line2 = addr.line2,
                city = addr.city,
                state = addr.state,
                pincode = addr.pincode
            )


            val orderItems = items.map {
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    orderId = orderId,
                    productId = it.productId,
                    name = it.name,
                    imageUrl = it.imageUrl,
                    unitPrice = it.unitPrice,
                    quantity = it.quantity,
                    lineTotal = it.lineTotal
                )
            }

            orderRepo.insertOrderWithItems(order, orderItems)

            // Clear ONLY this store cart
            cartRepo.clearStore(uid, storeId)

            _state.value = _state.value.copy(message = "Order placed successfully.")
        }
    }


    fun increaseQty(productId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        val item = _state.value.active.items.firstOrNull { it.productId == productId } ?: return

        viewModelScope.launch {
            cartRepo.setQty(uid, item.storeId, item.productId, item.quantity + 1)
        }
    }

    fun decreaseQty(productId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        val item = _state.value.active.items.firstOrNull { it.productId == productId } ?: return

        val newQty = (item.quantity - 1).coerceAtLeast(0)

        viewModelScope.launch {
            if (newQty <= 0) cartRepo.remove(uid, item.storeId, item.productId)
            else cartRepo.setQty(uid, item.storeId, item.productId, newQty)
        }
    }


    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
