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
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val category = doc.getString("category") ?: "Other"
            val distanceText = doc.getString("distanceText") ?: ""
            val imageUrl = doc.getString("imageUrl") ?: ""
            val locationName = doc.getString("locationName") ?: ""
            val isOpen = doc.getBoolean("isOpen") ?: false

           // Log.d(TAG, "getStores: $name, $category, $distanceText, $imageUrl, $locationName, $isOpen")

            StoreUiModel(
                id = id,
                name = name,
                category = category,
                distanceText = distanceText,
                imageUrl = imageUrl,
                locationName = locationName,
                isOpen = isOpen
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
