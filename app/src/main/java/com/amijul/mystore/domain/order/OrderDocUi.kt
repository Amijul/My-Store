package com.amijul.mystore.domain.order

data class OrderDocUi(
    val orderId: String,
    val storeId: String,
    val storeName: String,
    val status: String,
    val buyerName: String,
    val buyerPhone: String,
    val itemsTotal: Double,
    val shipping: Double,
    val grandTotal: Double,
    val addressText: String,
)

data class OrderItemDocUi(
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Double,
    val qty: Int,
    val lineTotal: Double
)
