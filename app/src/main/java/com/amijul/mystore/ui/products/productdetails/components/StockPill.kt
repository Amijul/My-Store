package com.amijul.mystore.ui.products.productdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StockPill(inStock: Boolean) {
    val bg = if (inStock) Color(0xFF2E7D32).copy(alpha = 0.14f) else Color(0xFFC62828).copy(alpha = 0.14f)
    val fg = if (inStock) Color(0xFF2E7D32) else Color(0xFFC62828)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (inStock) "In stock" else "Out of stock",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg
        )
    }
}