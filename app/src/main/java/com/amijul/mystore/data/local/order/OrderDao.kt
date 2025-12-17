package com.amijul.mystore.data.local.order


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeOrders(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        insertOrder(order)
        insertItems(items)
    }
}
