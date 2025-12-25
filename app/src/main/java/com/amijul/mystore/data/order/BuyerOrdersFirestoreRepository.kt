package com.amijul.mystore.data.order

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class BuyerOrderCardUi(
    val orderId: String,
    val storeId: String,
    val storeName: String,
    val status: String,
    val grandTotal: Double,
    val createdAtMillis: Long
)

class BuyerOrdersFirestoreRepository(
    private val db: FirebaseFirestore
) {
    fun observeActive(buyerId: String) = observeByStatuses(
        buyerId = buyerId,
        statuses = listOf("PLACED", "ACCEPTED")
    )

    fun observePast(buyerId: String) = observeByStatuses(
        buyerId = buyerId,
        statuses = listOf("REJECTED", "CANCELLED", "DELIVERED")
    )

    private fun observeByStatuses(buyerId: String, statuses: List<String>) =
        callbackFlow<List<BuyerOrderCardUi>> {

            var lastGood: List<BuyerOrderCardUi> = emptyList()

            val ref = db.collection("users")
                .document(buyerId)
                .collection("orders")
                .whereIn("status", statuses)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val reg: ListenerRegistration = ref.addSnapshotListener { snap, err ->
                if (err != null) {
                    // DO NOT clear list (prevents "appear then gone")
                    trySend(lastGood)
                    return@addSnapshotListener
                }

                val list = snap?.documents.orEmpty().map { d ->
                    val oid = d.getString("orderId").orEmpty().ifBlank { d.id }
                    val createdAtMillis =
                        d.getTimestamp("createdAt")?.toDate()?.time
                            ?: d.getTimestamp("updatedAt")?.toDate()?.time
                            ?: 0L

                    BuyerOrderCardUi(
                        orderId = oid,
                        storeId = d.getString("storeId").orEmpty(),
                        storeName = d.getString("storeName").orEmpty(),
                        status = d.getString("status").orEmpty(),
                        grandTotal = d.getDouble("grandTotal") ?: 0.0,
                        createdAtMillis = createdAtMillis
                    )
                }

                lastGood = list
                trySend(list)
            }

            awaitClose { reg.remove() }
        }

    // Optional: if you still want client cancel (rules allow mirror update)
    suspend fun cancelMirrorOnly(buyerId: String, orderId: String) {
        db.collection("users").document(buyerId)
            .collection("orders").document(orderId)
            .update(mapOf("status" to "CANCELLED"))
            .await()
    }
}
