package com.amijul.mystore.ui.account.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.local.user.UserEntity
import com.amijul.mystore.domain.user.UserLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUserProfileUiState(
    val isLoading: Boolean = true,
    val userId: String = "",

    val name: String = "",
    val email: String = "",
    val imageUrl: String? = null,

    val error: String? = null,
    val saved: Boolean = false
)

class EditUserProfileViewModel(
    private val userIdProvider: () -> String?,
    private val authEmailProvider: () -> String?,
    private val userRepo: UserLocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditUserProfileUiState())
    val state: StateFlow<EditUserProfileUiState> = _state.asStateFlow()

    fun load() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, saved = false, userId = uid) }

            val existing = userRepo.getUser(uid)
            if (existing != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = existing.name,
                        email = existing.email,
                        imageUrl = existing.imageUrl
                    )
                }
                return@launch
            }

            val seededEmail = authEmailProvider().orEmpty()

            val seed = UserEntity(
                userId = uid,
                name = "",
                email = seededEmail,
                imageUrl = null,
                phone = null,
                updatedAtMillis = System.currentTimeMillis()
            )

            userRepo.upsert(seed)

            _state.update {
                it.copy(
                    isLoading = false,
                    name = seed.name,
                    email = seed.email,
                    imageUrl = seed.imageUrl
                )
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v, error = null, saved = false) }

    fun setImageUrl(v: String?) = _state.update { it.copy(imageUrl = v, error = null, saved = false) }

    fun save() {
        val uid = _state.value.userId.ifBlank { userIdProvider().orEmpty() }
        if (uid.isBlank()) {
            _state.update { it.copy(error = "User not logged in") }
            return
        }

        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Please enter your name.") }
            return
        }

        viewModelScope.launch {
            val existing = userRepo.getUser(uid)

            val emailFinal =
                s.email.ifBlank { existing?.email.orEmpty() }
                    .ifBlank { authEmailProvider().orEmpty() }

            val entity = UserEntity(
                userId = uid,
                name = s.name.trim(),
                email = emailFinal.trim(),
                imageUrl = s.imageUrl,
                phone = existing?.phone,
                updatedAtMillis = System.currentTimeMillis()
            )

            userRepo.upsert(entity)
            _state.update { it.copy(saved = true, error = null) }
        }
    }
}
