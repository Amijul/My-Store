package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusPill(status: String) {
    val (bg, fg, text) = when (status.uppercase()) {
        "DELIVERED" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Delivered")
        else -> Triple(Color(0xFFFFF7ED), Color(0xFFB45309), "Pending")
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}