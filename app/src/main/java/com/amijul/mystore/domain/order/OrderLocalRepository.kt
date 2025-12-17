package com.amijul.mystore.domain.order


import com.amijul.mystore.data.local.order.OrderEntity
import com.amijul.mystore.data.local.order.OrderItemEntity
import kotlinx.coroutines.flow.Flow

interface OrderLocalRepository {
    fun observeOrders(userId: String): Flow<List<OrderEntity>>
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>)
}
