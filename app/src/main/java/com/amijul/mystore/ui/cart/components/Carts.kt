package com.amijul.mystore.ui.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amijul.mystore.domain.cart.CartUiState
import com.amijul.mystore.ui.products.productdetails.components.SwipeProceedButton

@Composable
fun Carts(
    state: CartUiState,
    onBack: () -> Unit,
    onIncreaseQty: (String) -> Unit,
    onDecreaseQty: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onProceedCheckout: () -> Unit
) {
    val bg = Color(0xFFD9E3FF)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp)) // symmetry
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { onIncreaseQty(item.id) },
                        onDecrease = { onDecreaseQty(item.id) },
                        onRemove = { onRemoveItem(item.id) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SummaryRow(label = "Sub total", value = "₹${state.subTotal}")
            Spacer(Modifier.height(6.dp))
            SummaryRow(label = "Shipping", value = "₹${state.shipping}")
            Spacer(Modifier.height(10.dp))
            SummaryRow(
                label = "Total",
                value = "₹${state.total}",
                valueWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            SwipeProceedButton(
                enabled = state.items.isNotEmpty(),
                onSwipeComplete = onProceedCheckout
            )
        }
    }
}
