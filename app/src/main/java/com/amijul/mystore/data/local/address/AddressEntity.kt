package com.amijul.mystore.data.local.address

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "addresses",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "isDefault"])
    ]
)

data class AddressEntity(
    @PrimaryKey val addressId: String,
    val userId: String,

    val fullName: String,
    val phone: String,

    val line1: String,
    val line2: String? = null,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
