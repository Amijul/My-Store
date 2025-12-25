package com.amijul.mystore.ui.orderdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.order.OrderDetailsViewModel

@Composable
fun OrderDetailsScreen(
    storeId: String,
    orderId: String,
    onBack: () -> Unit,
    vm: OrderDetailsViewModel
) {
    val bg = Color(0xFFD9E3FF)
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.start() }

    Box(Modifier.fillMaxSize().background(bg)) {

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Order Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            val order = s.order
            if (order == null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Loading your order…", fontWeight = FontWeight.Medium)
                    }
                }
                return
            }

            // Status + Store + Address
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Status: ${order.status}", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Store: ${order.storeName}")
                    Spacer(Modifier.height(10.dp))
                    Text("Delivery Address", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(order.addressText)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Items", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(s.items) { it ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(it.name, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("₹${it.unitPrice} × ${it.qty}")
                            }
                            Text("₹${it.lineTotal}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Bottom bar
        val order = s.order
        if (order != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(bg)
                    .padding(16.dp)
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Total: ₹${order.grandTotal}", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { vm.cancel() },
                    enabled = order.status == "PLACED",
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Cancel Order") }

                if (s.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
