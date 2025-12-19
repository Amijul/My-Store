package com.amijul.mystore.data.remote


import com.amijul.mystore.domain.order.OrderRemoteRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class OrderFunctionsRepository(
    private val functions: FirebaseFunctions
) : OrderRemoteRepository {

    override suspend fun createOrder(
        storeId: String,
        items: List<Pair<String, Int>>
    ): String {
        val payload = hashMapOf(
            "storeId" to storeId,
            "items" to items.map { (productId, qty) ->
                hashMapOf(
                    "productId" to productId,
                    "qty" to qty
                )
            }
        )

        val res = functions
            .getHttpsCallable("createOrder")
            .call(payload)
            .await()

        val map = res.data as Map<*, *>
        return map["orderId"] as? String ?: error("Missing orderId")
    }
}
