package com.amijul.mystore.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.domain.account.AccountUi
import com.amijul.mystore.domain.user.UserLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountState(
    val isLoading: Boolean = true,
    val accountUi: AccountUi = AccountUi(name = "", email = "", photoUrl = null),
    val error: String? = null
)

class AccountViewModel(
    private val userIdProvider: () -> String?,
    private val userLocalRepo: UserLocalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    fun start() {
        val uid = userIdProvider().orEmpty()
        if (uid.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            userLocalRepo.observeUser(uid)
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load user") }
                }
                .collectLatest { user ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            accountUi = AccountUi(
                                name = user?.name.orEmpty(),
                                email = user?.email.orEmpty(),
                                photoUrl = user?.imageUrl
                            )
                        )
                    }
                }
        }
    }
}
