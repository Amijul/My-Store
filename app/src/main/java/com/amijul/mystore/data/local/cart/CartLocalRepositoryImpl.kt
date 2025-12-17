package com.amijul.mystore.data.local.cart


import com.amijul.mystore.domain.cart.CartLocalRepository
import java.util.UUID

class CartLocalRepositoryImpl(
    private val dao: CartDao
) : CartLocalRepository {

    override fun observeCart(userId: String) = dao.observeCart(userId)

    override suspend fun addOrIncrease(
        userId: String,
        productId: String,
        name: String,
        imageUrl: String,
        unitPrice: Float,
        addQty: Int
    ) {
        val existing = dao.getByProduct(userId, productId)
        val newQty = (existing?.quantity ?: 0) + addQty

        dao.upsert(
            CartItemEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                userId = userId,
                productId = productId,
                name = name,
                imageUrl = imageUrl,
                unitPrice = unitPrice,
                quantity = newQty,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun setQty(userId: String, productId: String, newQty: Int) {
        val existing = dao.getByProduct(userId, productId) ?: return
        dao.upsert(
            existing.copy(
                quantity = newQty.coerceAtLeast(1),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun remove(userId: String, productId: String) = dao.remove(userId, productId)

    override suspend fun clear(userId: String) = dao.clear(userId)
}
