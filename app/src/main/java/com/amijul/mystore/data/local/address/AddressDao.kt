package com.amijul.mystore.data.local.address


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, updatedAtMillis DESC")
    fun observeAddresses(userId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, updatedAtMillis DESC")
    suspend fun getAddresses(userId: String): List<AddressEntity>

    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefault(userId: String): AddressEntity?

    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    fun observeDefault(userId: String): Flow<AddressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE addressId = :addressId")
    suspend fun delete(addressId: String)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefault(userId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE addressId = :addressId")
    suspend fun setDefault(addressId: String)

    @Transaction
    suspend fun setDefaultForUser(userId: String, addressId: String) {
        clearDefault(userId)
        setDefault(addressId)
    }

    @Query("DELETE FROM addresses WHERE userId = :userId")
    suspend fun clearUserAddresses(userId: String)
}
