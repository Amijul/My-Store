package com.amijul.mystore.ui.products.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amijul.mystore.domain.product.ProductUiModel
import kotlin.collections.set

@Composable
fun ProductListContent(
    products: List<ProductUiModel>
) {
    // quantity per productId
    val quantities = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(products) { product ->
            val qty = quantities[product.id] ?: 0

            ProductCard(
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
                    if (newValue == 0) {
                        quantities.remove(product.id)
                    } else {
                        quantities[product.id] = newValue
                    }
                }
            )
        }
    }
}