package com.amijul.mystore.data.local.order


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String,

    val storeId: String,
    val storeName: String,

    val status: String,            // "PENDING" / "DELIVERED"
    val createdAtMillis: Long,

    val itemsTotal: Float,
    val shipping: Float,
    val grandTotal: Float,

    // address snapshot at purchase time
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String?,
    val city: String,
    val state: String,
    val pincode: String
)

