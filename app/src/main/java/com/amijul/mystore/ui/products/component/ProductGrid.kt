package com.amijul.mystore.ui.products.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amijul.mystore.domain.product.ProductUiModel

@Composable
fun ProductGrid(
    products: List<ProductUiModel>,
    qtyByProductId: Map<String, Int>,
    onAdd: (ProductUiModel) -> Unit,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onProductClick: (ProductUiModel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(products, key = { it.id }) { product ->
            val qty = qtyByProductId[product.id] ?: 0

            ProductGridItem(
                product = product,
                quantity = qty,
                onOpenDetails = { onProductClick(product) },
                onAddFirstTime = { onAdd(product) },
                onIncrease = { onIncrease(product.id) },
                onDecrease = { onDecrease(product.id) }
            )
        }
    }
}
