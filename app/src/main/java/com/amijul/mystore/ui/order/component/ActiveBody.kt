package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amijul.mystore.ui.order.OrderUiState

@Composable
fun ActiveBody(
    state: OrderUiState,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onBuyNow: () -> Unit,
    isLoading: Boolean,
) {
    val active = state.active

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = true
    ) {
        item {
            PremiumCard {
                Text("Delivery Address", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(8.dp))

                if (!active.hasAddress) {
                    Text(
                        "No address found. Add one so the delivery guy doesn’t have to become Sherlock Holmes.",
                        color = Color(0xFF6B7280)
                    )
                } else {
                    Text(active.addressText, color = Color(0xFF111827))
                }
            }
        }

        item {
            Text(
                text = "Items",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        if (active.items.isEmpty()) {
            item {
                PremiumCard {
                    Text(
                        "Your cart is empty.\nGo add something and make your future self proud.",
                        color = Color(0xFF6B7280)
                    )
                }
            }
        } else {
            items(active.items) { it ->
                PremiumItemRow(
                    item = it,
                    onIncrease = { onIncrease(it.productId) },
                    onDecrease = { onDecrease(it.productId) }
                )
            }


            item {
                PremiumCard {
                    Text("Total", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(10.dp))
                    PriceRow("Subtotal", "₹${active.price.subTotal}")
                    PriceRow("Shipping", "₹${active.price.shipping}")
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFEAEAEA))
                    Spacer(Modifier.height(10.dp))
                    PriceRowStrong("Total", "₹${active.price.total}")
                }
            }

            item {
                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF515F86)),
                    enabled = !isLoading && state.active.items.isNotEmpty() && state.active.hasAddress
                ) {
                    Text("BUY", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }


        }
    }
}
