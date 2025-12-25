package com.amijul.mystore.data.remote

import android.util.Log
import com.amijul.mystore.domain.home.StoreUiModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


/**
 * DTO that matches your backend's JSON for a store.
 * Adjust field names / @SerialName to match your API.
 */

//private const val TAG = "StoreFirestoreDataSource"


class StoreFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {

    suspend fun getStores(): List<StoreUiModel> {
        val snapshot = firestore.collection("stores")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null

            val type = doc.getString("type").orEmpty().ifBlank { "other" }
            val phone = doc.getString("phone").orEmpty()
            val imageUrl = doc.getString("imageUrl").orEmpty()
            val isActive = doc.getBoolean("isActive") ?: true

            val address = doc.get("address") as? Map<*, *>
            val line1 = address?.get("line1") as? String ?: ""
            val city = address?.get("city") as? String ?: ""
            val state = address?.get("state") as? String ?: ""
            val pincode = address?.get("pincode") as? String ?: ""

            StoreUiModel(
                id = id,
                name = name,
                type = type,
                phone = phone,
                imageUrl = imageUrl,
                line1 = line1,
                city = city,
                state = state,
                pincode = pincode,
                isActive = isActive
            )
        }
    }


    companion object {
        fun default(): StoreFirestoreDataSource {
            return StoreFirestoreDataSource(
                firestore = FirebaseFirestore.getInstance()
            )
        }
    }
}
