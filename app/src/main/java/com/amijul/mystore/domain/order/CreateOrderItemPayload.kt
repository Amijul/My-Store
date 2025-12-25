package com.amijul.mystore.domain.order

data class CreateOrderItemPayload(
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Double,
    val qty: Int
)

data class CreateOrderAddressPayload(
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String?,
    val city: String,
    val state: String,
    val pincode: String
)

