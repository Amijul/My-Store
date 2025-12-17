package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        Text(value, color = Color(0xFF111827))
    }
}

@Composable
fun PriceRowStrong(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}