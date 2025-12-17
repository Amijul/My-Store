package com.amijul.mystore.ui.order.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.amijul.mystore.ui.order.OrderItemUi

@Composable
fun PremiumItemRow(
    item: OrderItemUi,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(14.dp))
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // minus
                    OutlinedButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(34.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("−", style = MaterialTheme.typography.titleMedium) }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(Modifier.width(10.dp))

                    // plus
                    Button(
                        onClick = onIncrease,
                        modifier = Modifier.size(34.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111827),
                            contentColor = Color.White
                        )
                    ) { Text("+", style = MaterialTheme.typography.titleMedium) }
                }
            }

            Spacer(Modifier.width(10.dp))

            Text(
                "₹${item.lineTotal}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF111827)
            )
        }
    }
}