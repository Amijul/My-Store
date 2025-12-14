package com.amijul.mystore.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    fun isLoggedIn(): Boolean
    fun signOut()
    fun authState(): Flow<Boolean>

}