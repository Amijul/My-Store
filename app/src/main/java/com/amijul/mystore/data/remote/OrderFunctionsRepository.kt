package com.amijul.mystore.data.remote


import com.amijul.mystore.domain.order.OrderRemoteRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class OrderFunctionsRepository(
    private val functions: FirebaseFunctions
) : OrderRemoteRepository {

    override suspend fun createOrder(
        storeId: String,
        storeName: String,
        buyerName: String,
        buyerPhone: String,
        itemsTotal: Double,
        shipping: Double,
        grandTotal: Double
    ): String {
        val data = hashMapOf(
            "storeId" to storeId,
            "storeName" to storeName,
            "buyerName" to buyerName,
            "buyerPhone" to buyerPhone,
            "itemsTotal" to itemsTotal,
            "shipping" to shipping,
            "grandTotal" to grandTotal
        )

        val res = functions
            .getHttpsCallable("createOrder")
            .call(data)
            .await()

        val map = res.data as Map<*, *>
        return map["orderId"] as String
    }
}
