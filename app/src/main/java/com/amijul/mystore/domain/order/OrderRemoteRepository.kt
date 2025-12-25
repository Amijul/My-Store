package com.amijul.mystore.domain.order

import com.amijul.mystore.data.local.address.AddressEntity
import com.amijul.mystore.data.local.cart.CartItemEntity


interface OrderRemoteRepository {
    suspend fun createOrder(
        storeId: String,
        storeName: String,
        address: AddressEntity,
        items: List<CartItemEntity>
    ): String
}
