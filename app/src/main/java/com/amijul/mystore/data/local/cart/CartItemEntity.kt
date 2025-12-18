package com.amijul.mystore.data.local.cart

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    indices = [
        Index(value = ["userId"]),
        // One user should not have duplicate rows for the same product within the same store
        Index(value = ["userId", "storeId", "productId"], unique = true)
    ]
)
data class CartItemEntity(
    @PrimaryKey val id: String, // UUID

    val userId: String,

    /**
     * Store context (critical for your architecture):
     * - Buyer selects a store -> browses that store's products -> adds to cart
     * - Later, you place an order routed to that seller/store
     */
    val storeId: String,
    val storeName: String,

    val productId: String,      // product id

    val name: String,
    val imageUrl: String,
    val unitPrice: Float,

    val quantity: Int,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
