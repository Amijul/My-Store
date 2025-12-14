package com.amijul.mystore.data.local.user


import com.amijul.mystore.domain.user.UserLocalRepository
import kotlinx.coroutines.flow.Flow

class UserLocalRepositoryImpl(
    private val userDao: UserDao
) : UserLocalRepository {

    override fun observeUser(userId: String): Flow<UserEntity?> = userDao.observeUser(userId)

    override suspend fun getUser(userId: String): UserEntity? = userDao.getUser(userId)

    override suspend fun upsert(user: UserEntity) = userDao.upsert(user)

    override suspend fun clearAll() = userDao.clearAll()
}
