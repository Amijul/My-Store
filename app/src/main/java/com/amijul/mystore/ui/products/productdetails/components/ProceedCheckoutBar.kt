package com.amijul.mystore.ui.products.productdetails.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProceedCheckoutBar(
    enabled: Boolean,
    onProceed: () -> Unit
) {
    // big capsule
    val shape = RoundedCornerShape(999.dp)

    // floating container (like screenshot)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // ✅ floating gap from edges
            .shadow(
                elevation = 18.dp,   // ✅ looks floating
                shape = shape,
                clip = false
            ),
        shape = shape,
        color = Color(0xFF0F1623) // ✅ deep navy/black
    ) {
        Button(
            onClick = onProceed,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = shape,
            // ✅ keep surface color visible; make button transparent
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proceed to checkout",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
