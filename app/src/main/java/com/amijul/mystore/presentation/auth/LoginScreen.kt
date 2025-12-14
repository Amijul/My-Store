package com.amijul.mystore.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoSignUp: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }

    val funLines = remember {
        listOf(
            "Welcome back. Your cart missed you.",
            "Login first, then we do shopping damage.",
            "Good taste starts with a good login.",
            "Let’s get you signed in—quickly."
        )
    }
    val funText = remember { funLines.random() }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFFF4F7FF), Color(0xFFF7F7F7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = funText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = authState.email,
                    onValueChange = authViewModel::onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = authState.password,
                    onValueChange = authViewModel::onPasswordChange,
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { authViewModel.sendPasswordReset() }) {
                        Text("Forgot password?")
                    }
                }




                if (authState.error != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = authState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val canSubmit =
                    !authState.isLoading && authState.email.isNotBlank() && authState.password.isNotBlank()

                Button(
                    onClick = { authViewModel.signIn(onSuccess = onLoggedIn) },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (authState.isLoading) "Signing you in..." else "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(Modifier.height(10.dp))

                TextButton(
                    onClick = onGoSignUp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("New here? Create an account")
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = "By continuing, you agree to our Terms & Privacy.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
