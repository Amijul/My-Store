package com.amijul.mystore.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val imageUrl: String? = null,
    val phone: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)