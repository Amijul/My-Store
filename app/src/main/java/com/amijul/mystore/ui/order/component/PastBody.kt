package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amijul.mystore.ui.order.OrderUiState

@Composable
fun PastBody(state: OrderUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.past.isEmpty()) {
            item {
                PremiumCard {
                    Text(
                        "No past orders yet.\nOnce you buy something, we’ll proudly display your shopping history here.",
                        color = Color(0xFF6B7280)
                    )
                }
            }
        } else {
            items(state.past, key = { it.orderId }) { o ->
                PremiumCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            o.orderId.take(8),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill(o.status)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(o.dateText, color = Color(0xFF6B7280))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Total: ₹${o.total}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
