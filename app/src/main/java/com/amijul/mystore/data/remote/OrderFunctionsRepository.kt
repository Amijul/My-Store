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
        address: com.amijul.mystore.data.local.address.AddressEntity,
        items: List<com.amijul.mystore.data.local.cart.CartItemEntity>
    ): String {

        val payload = hashMapOf(
            "storeId" to storeId,
            "storeName" to storeName,
            "address" to hashMapOf(
                "fullName" to address.fullName,
                "phone" to address.phone,
                "line1" to address.line1,
                "line2" to (address.line2 ?: ""),
                "city" to address.city,
                "state" to address.state,
                "pincode" to address.pincode
            ),
            "items" to items.map { it ->
                hashMapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "imageUrl" to it.imageUrl,
                    "unitPrice" to it.unitPrice,
                    "qty" to it.quantity
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
