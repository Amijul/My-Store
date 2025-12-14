package com.amijul.mystore.ui.account.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.amijul.mystore.ui.account.address.component.PremiumField
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreen(
    viewModel: EditUserProfileViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUrl(uri?.toString())
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9E3FF))
            .statusBarsPadding()
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
                text = "Profile",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        )
        {

            /* ---------------- CONTENT (WRAP CONTENT HEIGHT) ---------------- */
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape =  RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal =  8.dp)
                    .wrapContentHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // IMAGE SECTION FIRST (FULL AREA CLICKABLE)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .clickable(enabled = !state.isLoading) { imagePicker.launch("image/*") }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!state.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = state.imageUrl,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                )

                            } else {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Profile photo",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Tap Change to update",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }



                    PremiumField(
                        label = "Name",
                        value = state.name,
                        onValueChange = viewModel::setName,
                        placeholder = "Enter your name",
                        singleLine = true,
                        minLines = 1
                    )

                    PremiumField(
                        label = "Email",
                        value = state.email,
                        onValueChange = {},
                        placeholder = state.email,
                        singleLine = true,
                        minLines = 1
                    )

                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }

                    if (state.saved) {
                        Text("Saved", color = Color(0xFF2E7D32))
                    }
                }
            }

            // (Optional) If you want extra space below card before bottom bar
            Spacer(Modifier.height(12.dp))
        }

        /* ---------------- BOTTOM SAVE ---------------- */
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF515F86)),
                enabled = !state.isLoading
            ) {
                Text("SAVE")
            }
        }
    }
}
