package com.amijul.mystore.ui.account.address


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.address.AddressEntity
import com.amijul.mystore.domain.address.AddressLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditAddressUiState(
    val isLoading: Boolean = true,
    val addressId: String? = null,

    val fullName: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val isDefault: Boolean = true,

    val error: String? = null,
    val saved: Boolean = false
)

class EditAddressViewModel(
    private val userIdProvider: () -> String?,
    private val addressRepo: AddressLocalRepository,
    private val getDefaultAddress: suspend (String) -> AddressEntity? // injected helper
) : ViewModel() {

    private val _state = MutableStateFlow(EditAddressUiState())
    val state: StateFlow<EditAddressUiState> = _state.asStateFlow()

    fun load() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, saved = false) }

            val existing = getDefaultAddress(uid) // default address (or null)
            if (existing == null) {
                _state.update { it.copy(isLoading = false) }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        addressId = existing.addressId,
                        fullName = existing.fullName,
                        phone = existing.phone, // if you renamed to phone, change here
                        line1 = existing.line1,
                        line2 = existing.line2.orEmpty(),
                        city = existing.city,
                        state = existing.state,
                        pincode = existing.pincode,
                        isDefault = existing.isDefault
                    )
                }
            }
        }
    }

    // field setters
    fun setFullName(v: String) = _state.update { it.copy(fullName = v, error = null, saved = false) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v, error = null, saved = false) }
    fun setLine1(v: String) = _state.update { it.copy(line1 = v, error = null, saved = false) }
    fun setLine2(v: String) = _state.update { it.copy(line2 = v, error = null, saved = false) }
    fun setCity(v: String) = _state.update { it.copy(city = v, error = null, saved = false) }
    fun setState(v: String) = _state.update { it.copy(state = v, error = null, saved = false) }
    fun setPincode(v: String) = _state.update { it.copy(pincode = v, error = null, saved = false) }
    fun setDefault(v: Boolean) = _state.update { it.copy(isDefault = v, error = null, saved = false) }

    fun save() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.update { it.copy(error = "User not logged in") }
            return
        }

        val s = _state.value

        // minimal validation
        if (s.fullName.isBlank() || s.phone.isBlank() || s.line1.isBlank() ||
            s.city.isBlank() || s.state.isBlank() || s.pincode.isBlank()
        ) {
            _state.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            val addressId = s.addressId ?: UUID.randomUUID().toString()

            val entity = AddressEntity(
                addressId = addressId,
                userId = uid,
                fullName = s.fullName.trim(),
                phone = s.phone.trim(),  // if you renamed to phone, change here
                line1 = s.line1.trim(),
                line2 = s.line2.trim().ifBlank { null },
                city = s.city.trim(),
                state = s.state.trim(),
                pincode = s.pincode.trim(),
                isDefault = s.isDefault,
                updatedAtMillis = System.currentTimeMillis()
            )

            addressRepo.upsert(entity)

            // if default, enforce single default
            if (s.isDefault) {
                addressRepo.setDefaultForUser(uid, addressId)
            }

            _state.update { it.copy(addressId = addressId, saved = true, error = null) }
        }
    }
}
