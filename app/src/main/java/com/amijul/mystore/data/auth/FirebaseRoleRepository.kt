package com.amijul.mystore.data.auth


import com.amijul.mystore.domain.auth.RoleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseRoleRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : RoleRepository {

    override suspend fun setRoleAfterSignup(role: String) {
        val data = hashMapOf("role" to role)
        functions.getHttpsCallable("setRoleAfterSignup")
            .call(data)
            .await()
    }

    override suspend fun ensureBuyerRole() {
        val uid = auth.currentUser?.uid ?: return

        // Check Firestore role first (do not override sellers)
        val doc = db.collection("users").document(uid).get().await()
        val role = doc.getString("role")

        if (role == null || role == "unknown") {
            functions.getHttpsCallable("setRoleAfterSignup")
                .call(mapOf("role" to "buyer"))
                .await()

            // pull claims
            auth.currentUser?.getIdToken(true)?.await()
        }
    }
}
