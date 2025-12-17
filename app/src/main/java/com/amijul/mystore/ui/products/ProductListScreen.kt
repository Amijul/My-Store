package com.amijul.mystore.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.products.component.ProductGrid
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    cartViewModel: CartViewModel = koinViewModel(),
    storeName: String,
    onOpenProductDetail: () -> Unit
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9E3FF))
            .padding(16.dp)
    ) {


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
}







