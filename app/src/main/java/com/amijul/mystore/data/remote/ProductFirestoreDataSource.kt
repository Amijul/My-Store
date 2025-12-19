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
            .whereEqualTo("isActive", true) // safe even if missing (will exclude old docs)
            .get()
            .await()

        // If you have old docs without isActive, you can remove the whereEqualTo,
        // or keep it and ensure seller writes isActive=true.
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name")?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null

            val price = (doc.getDouble("price") ?: 0.0).toFloat()
            val inStock = doc.getBoolean("inStock") ?: true

            // Backward compatible image
            val imageUrl = pickBestImageUrl(doc)

            // Backward compatible unit:
            // - new schema: unit = { type: "kg", value: 1 }
            // - old schema: unit = "1kg"
            val unitText = buildUnitText(doc)

            ProductUiModel(
                id = id,
                name = name,
                price = price,
                unit = unitText,
                imageUrl = imageUrl,
                inStock = inStock
            )
        }
    }

    private fun pickBestImageUrl(doc: com.google.firebase.firestore.DocumentSnapshot): String {
        // New schema fields
        val thumbnail = doc.getString("thumbnail")
        if (!thumbnail.isNullOrBlank()) return thumbnail

        val images = doc.get("images")
        if (images is List<*>) {
            val first = images.firstOrNull() as? String
            if (!first.isNullOrBlank()) return first
        }

        // Old schema fallback
        val single = doc.getString("imageUrl")
        if (!single.isNullOrBlank()) return single

        return ""
    }

    private fun buildUnitText(doc: com.google.firebase.firestore.DocumentSnapshot): String {
        // New schema: unit: { type: "kg", value: 1 }
        val unitMap = doc.get("unit") as? Map<*, *>
        if (unitMap != null) {
            val type = unitMap["type"] as? String
            val valueAny = unitMap["value"]
            val value = when (valueAny) {
                is Number -> valueAny.toDouble()
                is String -> valueAny.toDoubleOrNull()
                else -> null
            }

            if (!type.isNullOrBlank() && value != null && value > 0) {
                val clean = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
                return "$clean$type"
            }
        }

        // Old schema: unit = "1kg" or "500g" etc
        return doc.getString("unit") ?: ""
    }
}
