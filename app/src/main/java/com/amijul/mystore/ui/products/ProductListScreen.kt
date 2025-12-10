package com.amijul.mystore.ui.products

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amijul.mystore.domain.product.ProductUiModel
import com.amijul.mystore.ui.products.component.ProductGridItem

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    storeName: String
) {
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

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
            .padding(16.dp)
    ) {

        // Top: store name
        Text(
            text = storeName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Browse items in this store.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

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
            placeholder = {
                Text("Search for items in $storeName")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                ProductGrid(products = filteredProducts)
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<ProductUiModel>
) {
    val quantities = remember { mutableStateMapOf<String, Int>() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(products) { product ->
            val qty = quantities[product.id] ?: 0
            ProductGridItem(
                product = product,
                quantity = qty,
                onAddFirstTime = {
                    quantities[product.id] = 1
                },
                onIncrease = {
                    val current = quantities[product.id] ?: 0
                    quantities[product.id] = current + 1
                },
                onDecrease = {
                    val current = quantities[product.id] ?: 0
                    val newValue = (current - 1).coerceAtLeast(0)
                    if (newValue == 0) quantities.remove(product.id) else quantities[product.id] = newValue
                }
            )
        }
    }
}




