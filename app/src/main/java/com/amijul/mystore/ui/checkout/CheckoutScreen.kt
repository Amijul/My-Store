package com.amijul.mystore.ui.checkout




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
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.cart.components.CartItemCard
import com.amijul.mystore.ui.cart.components.SummaryRow
import com.amijul.mystore.ui.order.OrderViewModel
import com.amijul.mystore.ui.products.productdetails.components.SwipeProceedButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CheckoutScreen(
    storeId: String,
    storeName: String,
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    orderViewModel: OrderViewModel = koinViewModel()
) {
    val bg = Color(0xFFD9E3FF)

    // Store-scoped cart VM
    val cartViewModel: CartViewModel = koinViewModel(
        parameters = { parametersOf(storeId, storeName) }
    )



    LaunchedEffect(storeId) {
        cartViewModel.start()
        orderViewModel.start()
    }

    val cartState by cartViewModel.state.collectAsStateWithLifecycle()
    val orderState by orderViewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
    ) {

        Spacer(Modifier.height(10.dp))

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Checkout",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
        }



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(10.dp))

            // Store name
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )

            Spacer(Modifier.height(12.dp))

            // Address
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Delivery Address",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(6.dp))

                    if (orderState.active.hasAddress) {
                        Text(
                            text = orderState.active.addressText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "No default address found. Please add one in Account → Address.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Payment (COD for now)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Cash on Delivery (COD)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items
            Text(
                text = "Items",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {
                items(cartState.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { cartViewModel.increase(item.id) },
                        onDecrease = { cartViewModel.decrease(item.id) },
                        onRemove = { cartViewModel.remove(item.id) }
                    )
                }
            }
        }

        // Bottom summary + place order
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SummaryRow(label = "Sub total", value = "₹${cartState.subTotal}")
            Spacer(Modifier.height(6.dp))
            SummaryRow(label = "Shipping fee", value = "₹${cartState.shipping}")
            Spacer(Modifier.height(10.dp))
            SummaryRow(
                label = "Total",
                value = "₹${cartState.total}",
                valueWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            SwipeProceedButton(
                title = "Proceed to Buy",
                enabled = cartState.items.isNotEmpty() && orderState.active.hasAddress,
                onSwipeComplete = {
                    orderViewModel.buyNow(
                        storeId = storeId,
                        storeName = storeName,
                        onSuccess = { orderId ->
                            onOrderPlaced(orderId)
                        }
                    )
                }
            )

        }
    }
}
