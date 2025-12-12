package com.amijul.mystore.ui.products.productdetails.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RatingStars(rating: Int) {
    // simple placeholder star row (classic style)
    Row {
        repeat(5) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
