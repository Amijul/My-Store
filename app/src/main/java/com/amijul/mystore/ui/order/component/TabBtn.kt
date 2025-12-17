package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TabBtn(selected: Boolean, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) Color(0xFF111827) else Color(0xFFF3F4F6)
    val fg = if (selected) Color.White else Color(0xFF111827)

    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}
