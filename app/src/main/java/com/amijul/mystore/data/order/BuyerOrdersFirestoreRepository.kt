package com.amijul.mystore.data.order

import com.amijul.mystore.domain.order.OrderDocUi
import com.amijul.mystore.domain.order.OrderItemDocUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BuyerOrdersFirestoreRepository(
    private val db: FirebaseFirestore
) {
    fun observeOrder(storeId: String, orderId: String) = callbackFlow<OrderDocUi?> {
        val ref = db.collection("stores").document(storeId)
            .collection("orders").document(orderId)

        val reg: ListenerRegistration = ref.addSnapshotListener { snap, _ ->
            if (snap == null || !snap.exists()) {
                trySend(null); return@addSnapshotListener
            }
            val addr = snap.get("address") as? Map<*, *>
            val addressText = buildString {
                val fullName = addr?.get("fullName") as? String ?: ""
                val phone = addr?.get("phone") as? String ?: ""
                val line1 = addr?.get("line1") as? String ?: ""
                val line2 = addr?.get("line2") as? String ?: ""
                val city = addr?.get("city") as? String ?: ""
                val state = addr?.get("state") as? String ?: ""
                val pincode = addr?.get("pincode") as? String ?: ""
                append(fullName)
                if (phone.isNotBlank()) append(" • $phone")
                append("\n$line1")
                if (line2.isNotBlank()) append(", $line2")
                append("\n$city, $state - $pincode")
            }

            trySend(
                OrderDocUi(
                    orderId = snap.getString("orderId").orEmpty(),
                    storeId = snap.getString("storeId").orEmpty(),
                    storeName = snap.getString("storeName").orEmpty(),
                    status = snap.getString("status").orEmpty(),
                    buyerName = snap.getString("buyerName").orEmpty(),
                    buyerPhone = snap.getString("buyerPhone").orEmpty(),
                    itemsTotal = snap.getDouble("itemsTotal") ?: 0.0,
                    shipping = snap.getDouble("shipping") ?: 0.0,
                    grandTotal = snap.getDouble("grandTotal") ?: 0.0,
                    addressText = addressText
                )
            )
        }

        awaitClose { reg.remove() }
    }

    fun observeOrderItems(storeId: String, orderId: String) = callbackFlow<List<OrderItemDocUi>> {
        val ref = db.collection("stores").document(storeId)
            .collection("orders").document(orderId)
            .collection("items")

        val reg = ref.addSnapshotListener { snap, _ ->
            val list = snap?.documents.orEmpty().map { d ->
                OrderItemDocUi(
                    productId = d.getString("productId").orEmpty(),
                    name = d.getString("name").orEmpty(),
                    imageUrl = d.getString("imageUrl").orEmpty(),
                    unitPrice = d.getDouble("unitPrice") ?: 0.0,
                    qty = (d.getLong("qty") ?: 0L).toInt(),
                    lineTotal = d.getDouble("lineTotal") ?: 0.0
                )
            }
            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    suspend fun cancelOrder(storeId: String, orderId: String) {
        db.collection("stores").document(storeId)
            .collection("orders").document(orderId)
            .update("status", "CANCELLED")
            .await()
    }
}
