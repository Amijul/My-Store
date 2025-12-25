package com.amijul.mystore.data.remote

import com.amijul.mystore.domain.order.CreateOrderAddressPayload
import com.amijul.mystore.domain.order.CreateOrderItemPayload
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class OrderRemoteRepositoryImpl(
    private val functions: FirebaseFunctions
) {
    suspend fun createOrder(
        storeId: String,
        storeName: String,
        items: List<CreateOrderItemPayload>,
        address: CreateOrderAddressPayload
    ): String {

        val payload = hashMapOf(
            "storeId" to storeId,
            "storeName" to storeName,
            "items" to items.map {
                hashMapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "imageUrl" to it.imageUrl,
                    "unitPrice" to it.unitPrice,
                    "qty" to it.qty
                )
            },
            "address" to hashMapOf(
                "fullName" to address.fullName,
                "phone" to address.phone,
                "line1" to address.line1,
                "line2" to address.line2,
                "city" to address.city,
                "state" to address.state,
                "pincode" to address.pincode
            )
        )

        val res = functions
            .getHttpsCallable("createOrder")
            .call(payload)
            .await()

        val map = res.data as Map<*, *>
        return map["orderId"] as? String ?: error("Missing orderId")
    }
}
