package com.amijul.mystore.domain.cart


import com.amijul.mystore.data.local.cart.CartItemEntity
import kotlinx.coroutines.flow.Flow

interface CartLocalRepository {
    fun observeCart(userId: String): Flow<List<CartItemEntity>>

    suspend fun addOrIncrease(
        userId: String,
        productId: String,
        name: String,
        imageUrl: String,
        unitPrice: Float,
        addQty: Int
    )

    suspend fun setQty(userId: String, productId: String, newQty: Int)
    suspend fun remove(userId: String, productId: String)
    suspend fun clear(userId: String)
}
