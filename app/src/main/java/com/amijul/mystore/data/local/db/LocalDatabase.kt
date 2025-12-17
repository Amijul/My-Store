package com.amijul.mystore.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.local.address.AddressEntity
import com.amijul.mystore.data.local.cart.CartDao
import com.amijul.mystore.data.local.cart.CartItemEntity
import com.amijul.mystore.data.local.order.OrderDao
import com.amijul.mystore.data.local.order.OrderEntity
import com.amijul.mystore.data.local.order.OrderItemEntity
import com.amijul.mystore.data.local.user.UserDao
import com.amijul.mystore.data.local.user.UserEntity


@Database(
    entities = [
        UserEntity::class,
        AddressEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 2,
    exportSchema = true

)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

}