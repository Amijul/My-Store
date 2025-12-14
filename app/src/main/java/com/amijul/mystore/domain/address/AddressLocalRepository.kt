package com.amijul.mystore.domain.address


import com.amijul.mystore.data.local.address.AddressEntity
import kotlinx.coroutines.flow.Flow

interface AddressLocalRepository {
    fun observeAddresses(userId: String): Flow<List<AddressEntity>>
    fun observeDefault(userId: String): Flow<AddressEntity?>
    suspend fun upsert(address: AddressEntity)
    suspend fun delete(addressId: String)
    suspend fun setDefaultForUser(userId: String, addressId: String)
    suspend fun clearUserAddresses(userId: String)
}
