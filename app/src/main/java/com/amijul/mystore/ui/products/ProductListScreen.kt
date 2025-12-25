package com.amijul.mystore.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.products.component.ProductGrid
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductListScreen(
    storeId: String,
    viewModel: ProductListViewModel,
    storeName: String,
    onGoToCart: () -> Unit,
    onOpenProductDetail: () -> Unit
) {

    val cartViewModel: CartViewModel = koinViewModel(
        parameters = { parametersOf(storeId, storeName) }
    )

    LaunchedEffect(Unit) {
        cartViewModel.start()
    }
    val cartState by cartViewModel.state.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val qtyByProductId = remember(cartState.items) {
        cartState.items.associate { it.id to it.quantity } // id = productId in your CartItemUi
    }
    // Filter products by name
    val filteredProducts = remember(searchQuery, state.products) {
        if (searchQuery.isBlank()) state.products
        else state.products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val cartItemCount = cartState.items.sumOf { it.quantity }
    val cartTotal = cartState.items.sumOf { ( // assumes price Float
            (it.price * it.quantity).toDouble() )
    }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9E3FF))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD9E3FF))
                .padding(16.dp)
        )
        {


            Spacer(modifier = Modifier.height(18.dp))

            // Search bar
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { /* TODO filters later */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filter"
                        )
                    }
                },
                placeholder = {
                    Text("Search for items in $storeName", maxLines = 1)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large, // more rounded
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Top: store name
            Text(
                text = storeName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = "Browse items in this store.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            when {
                state.isLoading -> {
                    Text(
                        text = "Loading products...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                state.errorMessage != null -> {
                    Text(
                        text = "Error: ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                else -> {
                    ProductGrid(
                        products = filteredProducts,
                        qtyByProductId = qtyByProductId,
                        contentPadding = PaddingValues(bottom = 110.dp),
                        onAdd = { p ->
                            cartViewModel.addToCart(
                                item = com.amijul.mystore.domain.cart.CartItemUi(
                                    id = p.id,
                                    name = p.name,
                                    price = p.price,
                                    quantity = 1,
                                    imageUrl = p.imageUrl
                                ),
                                qty = 1
                            )
                        },
                        onIncrease = { productId -> cartViewModel.increase(productId) },
                        onDecrease = { productId ->
                            val q = qtyByProductId[productId] ?: 0
                            if (q <= 1) cartViewModel.remove(productId) else cartViewModel.decrease(productId)
                        },
                        onProductClick = { product ->
                            viewModel.selectProduct(product.id)
                            onOpenProductDetail()
                        }
                    )


                }
            }
        }

        // Sticky bottom bar
        CartStickyBar(
            itemCount = cartItemCount,
            total = cartTotal,
            enabled = cartItemCount > 0,
            onGoToCart = onGoToCart,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )

    }


}




@Composable
private fun CartStickyBar(
    itemCount: Int,
    total: Float,
    enabled: Boolean,
    onGoToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (itemCount == 1) "1 item in cart" else "$itemCount items in cart",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Total: ₹${formatMoney(total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onGoToCart,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Go to Cart")
            }
        }
    }
}

private fun formatMoney(v: Float): String {
    // minimal formatting; replace with DecimalFormat if you prefer
    val i = v.toInt()
    return if (v == i.toFloat()) i.toString() else String.format("%.2f", v)
}



