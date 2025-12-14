package com.amijul.mystore.data.local.address


import com.amijul.mystore.domain.address.AddressLocalRepository
import kotlinx.coroutines.flow.Flow

class AddressLocalRepositoryImpl(
    private val addressDao: AddressDao
) : AddressLocalRepository {

    override fun observeAddresses(userId: String): Flow<List<AddressEntity>> =
        addressDao.observeAddresses(userId)

    override fun observeDefault(userId: String): Flow<AddressEntity?> =
        addressDao.observeDefault(userId)

    override suspend fun upsert(address: AddressEntity) = addressDao.upsert(address)

    override suspend fun delete(addressId: String) = addressDao.delete(addressId)

    override suspend fun setDefaultForUser(userId: String, addressId: String) =
        addressDao.setDefaultForUser(userId, addressId)

    override suspend fun clearUserAddresses(userId: String) =
        addressDao.clearUserAddresses(userId)
}
