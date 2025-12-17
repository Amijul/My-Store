package com.amijul.mystore.data.local.cart



import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "productId"], unique = true)
    ]
)
data class CartItemEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String,
    val productId: String,      // product id

    val name: String,
    val imageUrl: String,
    val unitPrice: Float,

    val quantity: Int,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
