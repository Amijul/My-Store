package com.amijul.mystore.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions

@Composable
fun SellerUpgradeScreen(
    onDone: () -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Upgrade to Seller", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Invite Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val code = inviteCode.trim()
                if (code.isEmpty()) {
                    status = "Enter invite code"
                    return@Button
                }

                loading = true
                status = null

                val functions = FirebaseFunctions.getInstance()
                val data: Map<String, Any> = mapOf("inviteCode" to code)

                functions
                    .getHttpsCallable("upgradeToSeller")
                    .call(data)
                    .addOnSuccessListener { result ->
                        val map = result.data as? Map<*, *>
                        val role = map?.get("role") as? String
                        val storeName = map?.get("storeName") as? String

                        FirebaseAuth.getInstance().currentUser
                            ?.getIdToken(true)
                            ?.addOnCompleteListener {
                                loading = false
                                status = "Upgraded: ${role ?: "seller"} (${storeName ?: "no store"})"
                                onDone()
                            }
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        status = e.message ?: "Failed"
                    }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Upgrading..." else "Upgrade")
        }

        Spacer(Modifier.height(12.dp))
        status?.let { Text(it) }
    }
}
