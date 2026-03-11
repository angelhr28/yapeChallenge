package com.angelhr28.yapechallenge.feature.detail.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Contenedor que permite hacer zoom y desplazamiento mediante gestos de pellizco y arrastre.
 *
 * @param modifier Modificador aplicado al contenedor.
 * @param minScale Escala minima permitida.
 * @param maxScale Escala maxima permitida.
 * @param content Contenido composable que sera ampliable.
 */
@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    val newOffset = if (newScale > 1f) {
                        Offset(
                            x = offset.x + pan.x,
                            y = offset.y + pan.y
                        )
                    } else {
                        Offset.Zero
                    }
                    scale = newScale
                    offset = newOffset
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
        ) {
            content()
        }
    }
}
