package com.amijul.mystore.domain.user


import com.amijul.mystore.data.local.user.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserLocalRepository {
    fun observeUser(userId: String): Flow<UserEntity?>
    suspend fun getUser(userId: String): UserEntity?
    suspend fun upsert(user: UserEntity)
    suspend fun clearAll()
}
