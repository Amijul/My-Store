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
    private val activeSet = setOf("PLACED", "ACCEPTED")
    private val pastSet = setOf("REJECTED", "CANCELLED", "DELIVERED")

    fun observeActive(buyerId: String) = observeAll(buyerId) { it.status in activeSet }
    fun observePast(buyerId: String) = observeAll(buyerId) { it.status in pastSet }

    private fun observeAll(
        buyerId: String,
        predicate: (BuyerOrderCardUi) -> Boolean
    ) = callbackFlow<List<BuyerOrderCardUi>> {

        var lastGood: List<BuyerOrderCardUi> = emptyList()

        val ref = db.collection("orders")
            .whereEqualTo("buyerId", buyerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val reg = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(lastGood)
                return@addSnapshotListener
            }

            val list = snap?.documents.orEmpty().map { d ->
                val oid = d.getString("orderId").orEmpty().ifBlank { d.id }
                val ts =
                    d.getTimestamp("createdAt")?.toDate()?.time
                        ?: d.getTimestamp("updatedAt")?.toDate()?.time
                        ?: 0L

                BuyerOrderCardUi(
                    orderId = oid,
                    storeId = d.getString("storeId").orEmpty(),
                    storeName = d.getString("storeName").orEmpty(),
                    status = d.getString("status").orEmpty(),
                    grandTotal = d.getDouble("grandTotal") ?: 0.0,
                    createdAtMillis = ts
                )
            }.filter(predicate)

            lastGood = list
            trySend(list)
        }

        awaitClose { reg.remove() }
    }
}

