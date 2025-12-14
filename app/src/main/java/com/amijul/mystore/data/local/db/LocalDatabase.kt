package com.amijul.mystore.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amijul.mystore.data.local.address.AddressDao
import com.amijul.mystore.data.local.address.AddressEntity
import com.amijul.mystore.data.local.user.UserDao
import com.amijul.mystore.data.local.user.UserEntity


@Database(
    entities = [
        UserEntity::class,
        AddressEntity::class
    ],
    version = 1,
    exportSchema = true

)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
}