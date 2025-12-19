package com.amijul.mystore.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.domain.auth.AuthRepository
import com.amijul.mystore.domain.auth.RoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val roleRepo: RoleRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()
    val loggedIn: StateFlow<Boolean> =
        authRepo.authState()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), authRepo.isLoggedIn())

    fun isLoggedIn(): Boolean = authRepo.isLoggedIn()

    fun onEmailChange(v: String) {
        _authState.value = _authState.value.copy(email = v, error = null)
    }

    fun onPasswordChange(v: String) {
        _authState.value = _authState.value.copy(password = v, error = null)
    }

    fun signOut() {
        authRepo.signOut()
    }

    fun signIn(onSuccess: () -> Unit) {
        val email = _authState.value.email.trim()
        val pass = _authState.value.password

        if (email.isBlank() || pass.isBlank()) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Email and password are required."
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                authRepo.signIn(email = email, password = pass) // ✅ awaited in repo
                roleRepo.ensureBuyerRole()
                _authState.value = _authState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace() // IMPORTANT: shows real cause in Logcat
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = mapAuthError(e)
                )
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        val email = _authState.value.email.trim()
        val pass = _authState.value.password

        if (email.isBlank() || pass.isBlank()) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Email and password are required."
            )
            return
        }

        if (pass.length < 6) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Password must be at least 6 characters."
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                authRepo.signUp(email = email, password = pass) // ✅ awaited in repo
                roleRepo.setRoleAfterSignup(role = "buyer")
                authRepo.refreshToken()
                _authState.value = _authState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = mapAuthError(e)
                )
            }
        }
    }

    fun sendPasswordReset() {
        val email = _authState.value.email.trim()

        if (email.isBlank()) {
            _authState.value = _authState.value.copy(
                error = "Email is required.",
                success = null
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            try {
                authRepo.sendPasswordReset(email)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    success = "Password reset email sent."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = mapAuthError(e)
                )
            }
        }
    }


    private fun mapAuthError(e: Exception): String
    {
        return when (e) {

            // 🔌 No internet / network issue
            is FirebaseNetworkException -> {
                "No internet connection. Please check your network."
            }

            // ❌ Invalid email OR wrong password
            is FirebaseAuthInvalidCredentialsException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email format."
                    "ERROR_WRONG_PASSWORD" -> "Wrong password."
                    else -> "Invalid email or password."
                }
            }

            // 👤 User does not exist / disabled
            is FirebaseAuthInvalidUserException -> {
                when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "Account not found."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    else -> "Account not available."
                }
            }

            // 📧 Email already registered
            is FirebaseAuthUserCollisionException -> {
                "Email already registered."
            }


            // ⏳ Too many attempts / rate limit
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Too many attempts. Please try again later."
                    else ->
                        "Authentication failed. Please try again."
                }
            }

            // ❓ Unknown error
            else -> {
                "Something went wrong. Please try again."
            }
        }
    }

}
