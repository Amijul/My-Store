package com.amijul.mystore.data.local.cart



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY updatedAtMillis DESC")
    fun observeCart(userId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getByProduct(userId: String, productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun remove(userId: String, productId: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clear(userId: String)
}
