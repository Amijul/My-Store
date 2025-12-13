package com.amijul.mystore.ui.cart.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StepCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val border = Color(0xFFCBD5E1)
    val content = Color(0xFF111827)

    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier
            .size(26.dp)
            .border(1.dp, border, CircleShape)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) content else content.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
