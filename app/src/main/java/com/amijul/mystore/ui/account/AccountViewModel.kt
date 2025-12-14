package com.amijul.mystore.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.address.AddressEntity
import com.amijul.mystore.data.local.user.UserEntity
import com.amijul.mystore.domain.address.AddressLocalRepository
import com.amijul.mystore.domain.user.UserLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountLocalUiState(
    val isLoading: Boolean = true,
    val user: UserEntity? = null,
    val addresses: List<AddressEntity> = emptyList(),
    val defaultAddress: AddressEntity? = null,
    val error: String? = null
)

class AccountViewModel(
    private val userIdProvider: () -> String?,
    private val userLocalRepo: UserLocalRepository,
    private val addressLocalRepo: AddressLocalRepository
) : ViewModel() {

    private fun userLocalFlow(): Flow<UserEntity?> {
        val uid = userIdProvider().orEmpty()
        return if (uid.isBlank()) flowOf(null)
        else userLocalRepo.observeUser(uid)
    }

    private fun addressesFlow(): Flow<List<AddressEntity>> {
        val uid = userIdProvider().orEmpty()
        return if (uid.isBlank()) flowOf<List<AddressEntity>>(emptyList())
        else addressLocalRepo.observeAddresses(uid)
    }

    private fun defaultAddressFlow(): Flow<AddressEntity?> {
        val uid = userIdProvider().orEmpty()
        return if (uid.isBlank()) flowOf(null)
        else addressLocalRepo.observeDefault(uid)
    }

    val accountState: StateFlow<AccountLocalUiState> =
        combine(
            userLocalFlow(),
            addressesFlow(),
            defaultAddressFlow()
        ) { user, addresses, defaultAddress ->
            AccountLocalUiState(
                isLoading = false,
                user = user,
                addresses = addresses,
                defaultAddress = defaultAddress,
                error = null
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountLocalUiState()
        )

    fun setDefaultAddress(addressId: String) {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) return

        viewModelScope.launch {
            addressLocalRepo.setDefaultForUser(uid, addressId)
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            addressLocalRepo.delete(addressId)
        }
    }

    fun upsertLocalUser(user: UserEntity) {
        viewModelScope.launch {
            userLocalRepo.upsert(user)
        }
    }
}
