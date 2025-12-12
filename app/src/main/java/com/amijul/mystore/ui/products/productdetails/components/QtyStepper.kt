package com.amijul.mystore.ui.products.productdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QtyStepper(
    qty: Int,
    enabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrease, enabled = enabled) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(
            text = qty.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        IconButton(onClick = onIncrease, enabled = enabled) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}
