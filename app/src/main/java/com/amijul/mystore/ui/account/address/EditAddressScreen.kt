package com.amijul.mystore.ui.account.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.account.address.component.PremiumField
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    viewModel: EditAddressViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9E3FF))
            .statusBarsPadding()
    ) {

        // ✅ Your screen content stays exactly the same layout/scroll behavior
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            /* ---------------- TOP BAR ---------------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Address",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
            }

            Spacer(Modifier.height(12.dp))

            /* ---------------- MAP PREVIEW (FIXED) ---------------- */
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("Map preview", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Location selection coming soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            /* ---------------- FORM (SCROLLABLE ONLY) ---------------- */


            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        "Address details",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )

                    PremiumField(
                        label = "Full name",
                        value = state.fullName,
                        onValueChange = viewModel::setFullName,
                        placeholder = "Enter your name"
                    )

                    PremiumField(
                        label = "Phone number",
                        value = state.phone,
                        onValueChange = viewModel::setPhone,
                        placeholder = "10-digit mobile number"
                    )

                    PremiumField(
                        label = "Address line 1",
                        value = state.line1,
                        onValueChange = viewModel::setLine1,
                        placeholder = "House no, street, area",
                        singleLine = false,
                        minLines = 2
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            PremiumField(
                                label = "Address line 2",
                                value = state.line2,
                                onValueChange = viewModel::setLine2,
                                placeholder = "Landmark",
                                singleLine = true,
                                minLines = 1
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            PremiumField(
                                label = "Pincode",
                                value = state.pincode,
                                onValueChange = viewModel::setPincode,
                                placeholder = "Postal code"
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            PremiumField(
                                label = "City",
                                value = state.city,
                                onValueChange = viewModel::setCity,
                                placeholder = "City"
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            PremiumField(
                                label = "State",
                                value = state.state,
                                onValueChange = viewModel::setState,
                                placeholder = "State"
                            )
                        }
                    }

                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Transparent) // ✅ this is what prevents it looking like it's sitting on the card
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF515F86),
                ),
                enabled = !state.isLoading
            ) {
                Text("SAVE ADDRESS")
            }
        }
    }
}
