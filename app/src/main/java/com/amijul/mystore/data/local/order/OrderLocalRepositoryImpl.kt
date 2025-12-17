package com.amijul.mystore.data.local.order


import com.amijul.mystore.domain.order.OrderLocalRepository

class OrderLocalRepositoryImpl(
    private val dao: OrderDao
) : OrderLocalRepository {

    override fun observeOrders(userId: String) = dao.observeOrders(userId)

    override suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        dao.insertOrderWithItems(order, items)
    }
}
