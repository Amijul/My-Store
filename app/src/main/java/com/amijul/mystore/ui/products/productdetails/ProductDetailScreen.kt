package com.amijul.mystore.ui.products.productdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.amijul.mystore.domain.product.ProductUiModel
import com.amijul.mystore.ui.products.ProductListViewModel
import com.amijul.mystore.ui.products.productdetails.components.ProceedCheckoutBar
import com.amijul.mystore.ui.products.productdetails.components.QtyStepper
import com.amijul.mystore.ui.products.productdetails.components.RatingStars
import com.amijul.mystore.ui.products.productdetails.components.StockPill
import com.amijul.mystore.ui.products.productdetails.components.SwipeProceedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    storeName: String,
    viewModel: ProductListViewModel,
    onBack: () -> Unit,
    onAddToCart: (ProductUiModel, Int) -> Unit,
    onProceedToCheckout: () -> Unit
) {
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()

    if (product == null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(storeName) }) }
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                Text("Product not found. Please go back.")
            }
        }
        return
    }

    val p = product!!
    var qty by remember { mutableIntStateOf(1) }

    val cardShape = RoundedCornerShape(22.dp)
    val pageBg = Color(0xFFD9E3FF)

    // ✅ Height of floating bar area (for safe scroll padding)
    val floatingBarSpace = 92.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(top = 22.dp)
    ) {

        // ✅ Scrollable content (add bottom padding so it won't hide behind bar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = floatingBarSpace)
        ) {
            Spacer(Modifier.height(8.dp))

            AsyncImage(
                model = p.imageUrl.takeIf { it.isNotBlank() },
                contentDescription = p.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(cardShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = p.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { /* wishlist later */ }) {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = "Wishlist")
                }
            }

            Text(
                text = "You can add description, brand, expiry, delivery info later from Seller app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(rating = 5)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "5.0",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${(p.price.toInt() + 580)}",
                    style = MaterialTheme.typography.bodyMedium.merge(
                        TextStyle(textDecoration = TextDecoration.LineThrough)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "₹${p.price.toInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )

                Spacer(Modifier.weight(1f))

                StockPill(inStock = p.inStock)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QtyStepper(
                    qty = qty,
                    enabled = p.inStock,
                    onDecrease = { qty = (qty - 1).coerceAtLeast(1) },
                    onIncrease = { qty = (qty + 1).coerceAtMost(99) }
                )

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { onAddToCart(p, qty) },
                    enabled = p.inStock,
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to cart", style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ✅ Floating Proceed button overlay (NOT Scaffold bottomBar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            SwipeProceedButton(
                enabled = p.inStock,
                onSwipeComplete = {
                    onProceedToCheckout()
                }
            )
        }

    }
}
