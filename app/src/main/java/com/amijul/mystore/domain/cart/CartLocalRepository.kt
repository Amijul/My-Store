package com.amijul.mystore.domain.cart

import com.amijul.mystore.data.local.cart.CartItemEntity
import kotlinx.coroutines.flow.Flow

interface CartLocalRepository {

    fun observeCart(userId: String, storeId: String): Flow<List<CartItemEntity>>

    // NEW: one-shot fetch (checkout)
    suspend fun getCartOnce(userId: String, storeId: String): List<CartItemEntity>

    suspend fun addOrIncrease(
        userId: String,
        storeId: String,
        storeName: String,
        productId: String,
        name: String,
        imageUrl: String,
        unitPrice: Float,
        addQty: Int = 1
    )

    suspend fun setQty(
        userId: String,
        storeId: String,
        productId: String,
        newQty: Int
    )

    suspend fun remove(
        userId: String,
        storeId: String,
        productId: String
    )

    suspend fun clearStore(userId: String, storeId: String)

    suspend fun clearAll(userId: String)
}
