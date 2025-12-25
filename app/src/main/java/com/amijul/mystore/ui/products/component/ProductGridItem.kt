package com.amijul.mystore.ui.products.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amijul.mystore.domain.product.ProductUiModel

@Composable
fun ProductGridItem(
    product: ProductUiModel,
    quantity: Int,
    onOpenDetails: () -> Unit,
    onAddFirstTime: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)

    val inStock = product.inStock && product.stockQty > 0
    val stockText = if (inStock) "Stock: ${product.stockQty}" else "Out of stock"
    val stockBg = if (inStock) Color(0xFFE8F7EE) else Color(0xFFFFE7E7)
    val stockFg = if (inStock) Color(0xFF166534) else Color(0xFF991B1B)

    val inCart = quantity > 0
    val enabled = product.inStock


    ElevatedCard(
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable { onOpenDetails() }
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                val img = product.imageUrl.trim()
                if (img.isNotBlank()) {
                    AsyncImage(
                        model = img,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = stockBg
                ) {
                    Text(
                        text = stockText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = stockFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price row: price + unit, mrp struck-through if valid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${formatMoney(product.price)}${unitSuffix(product.unit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (product.mrp > 0f && product.mrp > product.price) {
                            Text(
                                text = "MRP ₹${formatMoney(product.mrp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }


                    }


                }

                // ✅ Real cart actions: add / increase / decrease
                if (!inCart) {
                    Button(
                        enabled = enabled,
                        onClick = {
                            if (!enabled) return@Button
                            onAddFirstTime()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Add to cart",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (enabled) "Add to cart" else "Out of stock")
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        IconButton(
                            enabled = enabled,
                            onClick = {
                                if (!enabled) return@IconButton
                                onDecrease()
                            }

                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Decrease"
                            )
                        }

                        Text(
                            text = "Qty: $quantity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            enabled = enabled,
                            onClick = {
                                if (!enabled) return@IconButton
                                onIncrease()
                            }

                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Increase"
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun unitSuffix(unit: String): String {
    val u = unit.trim()
    return if (u.isBlank()) "" else " / $u"
}

private fun formatMoney(v: Float): String {
    val i = v.toInt()
    return if (v == i.toFloat()) i.toString() else String.format("%.2f", v)
}
