package com.amijul.mystore.domain.order


interface OrderRemoteRepository {
    suspend fun createOrder(
        storeId: String,
        storeName: String,
        buyerName: String,
        buyerPhone: String,
        itemsTotal: Double,
        shipping: Double,
        grandTotal: Double
    ): String
}
