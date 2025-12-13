package com.amijul.mystore.data.remote


import com.amijul.mystore.domain.product.ProductUiModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {

    suspend fun getProductsForStore(storeId: String): List<ProductUiModel> {

        val snapshot = firestore
            .collection("stores")
            .document(storeId)
            .collection("products")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val priceNumber = doc.getDouble("price") ?: 0.0
            val unit = doc.getString("unit") ?: ""
            val imageUrl = doc.getString("imageUrl") ?: ""
            val inStock = doc.getBoolean("inStock") ?: true

            ProductUiModel(
                id = id,
                name = name,
                price = priceNumber.toFloat(),
                unit = unit,
                imageUrl = imageUrl,
                inStock = inStock
            )
        }
    }
}




