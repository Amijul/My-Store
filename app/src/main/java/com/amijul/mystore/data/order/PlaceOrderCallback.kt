package com.amijul.mystore.data.order

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await

suspend fun placeOrderCallback(
    sellerId: String,
    items: List<Map<String, Any>>
): String {

    val data = hashMapOf(
        "sellerId" to sellerId,
        "items" to items
    )

    val result = Firebase.functions
        .getHttpsCallable("createOrder")
        .call(data)
        .await()

    val map = result.data as Map<*, *>
    if (map["ok"] != true) error("Order failed")
    return map["orderId"] as String
}