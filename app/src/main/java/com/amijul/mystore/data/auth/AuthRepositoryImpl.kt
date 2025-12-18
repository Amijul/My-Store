package com.amijul.mystore.data.auth

import com.amijul.mystore.domain.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
) : AuthRepository{
    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun authState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {fAuth ->
            trySend(fAuth.currentUser != null)
        }

        auth.addAuthStateListener(listener)
        trySend(auth.currentUser != null)
        awaitClose{ auth.removeAuthStateListener(listener) }
    }
    override fun signOut() {
       auth.signOut()
    }
}