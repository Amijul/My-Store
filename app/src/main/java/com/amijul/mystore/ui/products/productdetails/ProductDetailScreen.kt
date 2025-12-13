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
import com.amijul.mystore.domain.cart.CartItemUi
import com.amijul.mystore.domain.product.ProductUiModel
import com.amijul.mystore.ui.cart.CartViewModel
import com.amijul.mystore.ui.products.ProductListViewModel
import com.amijul.mystore.ui.products.productdetails.components.QtyStepper
import com.amijul.mystore.ui.products.productdetails.components.RatingStars
import com.amijul.mystore.ui.products.productdetails.components.StockPill
import com.amijul.mystore.ui.products.productdetails.components.SwipeProceedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    storeName: String,
    viewModel: ProductListViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onNavigation: () -> Unit,
) {
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val cartState by cartViewModel.state.collectAsStateWithLifecycle()
    val cartQty = cartState.items.firstOrNull { it.id == product?.id }?.quantity ?: 0

    val cardShape = RoundedCornerShape(22.dp)
    val pageBg = Color(0xFFD9E3FF)

    // ✅ Height of floating bar area (for safe scroll padding)
    val floatingBarSpace = 92.dp



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
    } else {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(top = 22.dp)
        )
        {

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
                    model = product!!.imageUrl.takeIf { it.isNotBlank() },
                    contentDescription = product?.name,
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
                    product?.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

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
                        text = "₹${(product!!.price.toInt() + 580)}",
                        style = MaterialTheme.typography.bodyMedium.merge(
                            TextStyle(textDecoration = TextDecoration.LineThrough)
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "₹${product?.price?.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(Modifier.weight(1f))

                    StockPill(inStock = product!!.inStock)
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QtyStepper(
                        qty = cartQty,                  // ✅ show real cart qty (0 allowed)
                        enabled = product!!.inStock,
                        onDecrease = {
                            if (!product!!.inStock) return@QtyStepper
                            if (cartQty <= 0) return@QtyStepper

                            if (cartQty == 1) cartViewModel.remove(product!!.id)
                            else cartViewModel.decrease(product!!.id)
                        },
                        onIncrease = {
                            if (!product!!.inStock) return@QtyStepper

                            if (cartQty == 0) {
                                cartViewModel.addToCart(
                                    CartItemUi(
                                        id = product!!.id,
                                        name = product!!.name,
                                        price = product!!.price,
                                        imageUrl = product!!.imageUrl,
                                        quantity = 1
                                    ),
                                    qty = 1
                                )
                            } else {
                                cartViewModel.increase(product!!.id)
                            }
                        }
                    )


                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (!product!!.inStock) return@Button
                            if (cartQty == 0) {
                                cartViewModel.addToCart(
                                    CartItemUi(
                                        id = product!!.id,
                                        name = product!!.name,
                                        price = product!!.price,
                                        imageUrl = product!!.imageUrl,
                                        quantity = 1
                                    ),
                                    qty = 1
                                )
                            }
                            onNavigation()
                        },
                        enabled = product!!.inStock,
                        modifier = Modifier.height(48.dp).weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (cartQty == 0) "Add to cart" else "Go to cart")
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
                    enabled = product!!.inStock,
                    onSwipeComplete = {
                        onProceedToCheckout()
                    }
                )
            }

        }
    }





}
