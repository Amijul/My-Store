package com.amijul.mystore.data.local.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query(
        "SELECT * FROM cart_items " +
                "WHERE userId = :userId AND storeId = :storeId " +
                "ORDER BY updatedAtMillis DESC"
    )
    fun observeCart(userId: String, storeId: String): Flow<List<CartItemEntity>>

    // NEW: one-shot fetch (used for checkout/order creation)
    @Query(
        "SELECT * FROM cart_items " +
                "WHERE userId = :userId AND storeId = :storeId " +
                "ORDER BY updatedAtMillis DESC"
    )
    suspend fun getCartOnce(userId: String, storeId: String): List<CartItemEntity>

    @Query("SELECT storeId FROM cart_items WHERE userId = :userId LIMIT 1")
    suspend fun getAnyStoreId(userId: String): String?

    @Query(
        "SELECT * FROM cart_items " +
                "WHERE userId = :userId AND storeId = :storeId AND productId = :productId " +
                "LIMIT 1"
    )
    suspend fun getByProduct(userId: String, storeId: String, productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query(
        "DELETE FROM cart_items " +
                "WHERE userId = :userId AND storeId = :storeId AND productId = :productId"
    )
    suspend fun remove(userId: String, storeId: String, productId: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND storeId = :storeId")
    suspend fun clearStore(userId: String, storeId: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
