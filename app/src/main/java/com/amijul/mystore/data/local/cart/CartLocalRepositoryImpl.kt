package com.amijul.mystore.data.local.cart

import com.amijul.mystore.domain.cart.CartLocalRepository
import java.util.UUID

class CartLocalRepositoryImpl(
    private val dao: CartDao
) : CartLocalRepository {

    override fun observeCart(userId: String, storeId: String) =
        dao.observeCart(userId, storeId)

    override suspend fun getCartOnce(userId: String, storeId: String): List<CartItemEntity> =
        dao.getCartOnce(userId, storeId)

    override suspend fun addOrIncrease(
        userId: String,
        storeId: String,
        storeName: String,
        productId: String,
        name: String,
        imageUrl: String,
        unitPrice: Float,
        addQty: Int
    ) {
        // Enforce single-store cart
        val existingStoreId = dao.getAnyStoreId(userId)
        if (existingStoreId != null && existingStoreId != storeId) {
            dao.clearAll(userId)
        }

        val existing = dao.getByProduct(userId, storeId, productId)
        val newQty = (existing?.quantity ?: 0) + addQty

        dao.upsert(
            CartItemEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                userId = userId,
                storeId = storeId,
                storeName = storeName,
                productId = productId,
                name = name,
                imageUrl = imageUrl,
                unitPrice = unitPrice,
                quantity = newQty.coerceAtLeast(1),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun setQty(userId: String, storeId: String, productId: String, newQty: Int) {
        val existing = dao.getByProduct(userId, storeId, productId) ?: return
        dao.upsert(
            existing.copy(
                quantity = newQty.coerceAtLeast(1),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun remove(userId: String, storeId: String, productId: String) =
        dao.remove(userId, storeId, productId)

    override suspend fun clearStore(userId: String, storeId: String) =
        dao.clearStore(userId, storeId)

    override suspend fun clearAll(userId: String) =
        dao.clearAll(userId)
}
