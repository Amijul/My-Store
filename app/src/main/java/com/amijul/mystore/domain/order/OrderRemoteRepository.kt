package com.amijul.mystore.domain.order


interface OrderRemoteRepository {
    suspend fun createOrder(
        storeId: String,
        items: List<Pair<String, Int>> // productId to qty
    ): String // orderId
}
