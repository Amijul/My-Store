package com.amijul.mystore.data.local.order


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    indices = [Index(value = ["userId"]), Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orderId: String,

    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Float,
    val quantity: Int,
    val lineTotal: Float
)

