package com.amijul.mystore.ui.order


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.local.order.OrderEntity
import com.amijul.mystore.data.local.order.OrderItemEntity
import com.amijul.mystore.domain.cart.CartLocalRepository
import com.amijul.mystore.domain.order.OrderLocalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OrderItemUi(
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Float,
    val quantity: Int
) {
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
            _state.value = OrderUiState(isLoading = false, message = "User not logged in")
            return
        }

        viewModelScope.launch {
            combine(
                addressDao.observeDefault(uid),
                cartRepo.observeCart(uid),
                orderRepo.observeOrders(uid)
            ) { addr, cart, orders ->

                val items = cart.map {
                    OrderItemUi(
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
                        itemCount = 0, // (optional) we can compute later by joining items table
                        total = o.grandTotal
                    )
                }

                OrderUiState(
                    isLoading = false,
                    active = ActiveOrderUi(
                        addressText = addressText,
                        hasAddress = addr != null,
                        items = items,
                        price = OrderPriceUi(subTotal = subTotal, shipping = shipping, total = total)
                    ),
                    past = pastUi,
                    message = null
                )
            }.collectLatest { newState ->
                _state.value = newState
            }
        }
    }

    fun buyNow() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        val current = _state.value
        if (!current.active.hasAddress) {
            _state.value = current.copy(message = "Add a delivery address first.")
            return
        }
        if (current.active.items.isEmpty()) {
            _state.value = current.copy(message = "Cart is empty. Add something tasty first.")
            return
        }

        viewModelScope.launch {
            val addr = addressDao.getDefault(uid) ?: run {
                _state.value = _state.value.copy(message = "No default address found.")
                return@launch
            }

            val orderId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()

            val subTotal = current.active.price.subTotal
            val shipping = current.active.price.shipping
            val total = current.active.price.total

            val order = OrderEntity(
                orderId = orderId,
                userId = uid,
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

            val items = current.active.items.map {
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

            orderRepo.insertOrderWithItems(order, items)

            // clear cart after successful order save
            cartRepo.clear(uid)

            _state.value = _state.value.copy(message = "Order placed. Now we wait like champions.")
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
