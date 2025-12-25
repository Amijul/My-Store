package com.amijul.mystore.ui.products.productdetails.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeProceedButton(
    title: String ,
    enabled: Boolean,
    onSwipeComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val trackHeight = 56.dp
    val thumbSize = 48.dp
    val shape = RoundedCornerShape(999.dp)

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbOffsetX = remember { Animatable(0f) }

    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSize.toPx() }

    val maxOffset by remember(trackWidthPx, thumbSizePx) {
        derivedStateOf { (trackWidthPx - thumbSizePx).coerceAtLeast(0f) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .shadow(18.dp, shape),
        shape = shape,
        color = Color(0xFF0F1623)
    ) {
        Box(
            modifier = Modifier
                .height(trackHeight)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
        ) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetX.value.roundToInt(), 0) }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color(0xFF234078))
                    .border(width = 1.dp, color = Color(0xFF6C87BB), shape = CircleShape)
                    .align(Alignment.CenterStart)
                    .pointerInput(enabled, maxOffset) {
                        if (!enabled) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (thumbOffsetX.value > maxOffset * 0.8f) {
                                    onSwipeComplete()
                                }
                                scope.launch {
                                    thumbOffsetX.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        ) { _, dragAmount ->
                            val newValue = (thumbOffsetX.value + dragAmount).coerceIn(0f, maxOffset)
                            scope.launch { thumbOffsetX.snapTo(newValue) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Proceed",
                    tint = Color(0xFF6C87BB)
                )
            }
        }
    }
}


