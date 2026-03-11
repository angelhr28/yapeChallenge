package com.angelhr28.yapechallenge.feature.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

/**
 * Superposicion de marca de agua con texto rotado y repetido sobre un Canvas.
 *
 * @param locationText Texto de ubicacion que se incluye en la marca de agua.
 * @param modifier Modificador aplicado al Canvas.
 * @param color Color del texto de la marca de agua.
 */
@Composable
fun WatermarkOverlay(
    locationText: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.3f)
) {
    val textMeasurer = rememberTextMeasurer()
    val watermarkText = "YapeChallenge | $locationText"

    Canvas(modifier = modifier.fillMaxSize()) {
        val textStyle = TextStyle(
            fontSize = 14.sp,
            color = color
        )

        val textLayoutResult = textMeasurer.measure(watermarkText, textStyle)
        val textWidth = textLayoutResult.size.width.toFloat()
        val textHeight = textLayoutResult.size.height.toFloat()

        val spacingX = textWidth + 80f
        val spacingY = textHeight + 120f

        rotate(degrees = -30f) {
            var y = -size.height
            while (y < size.height * 2) {
                var x = -size.width
                while (x < size.width * 2) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = watermarkText,
                        style = textStyle,
                        topLeft = Offset(x, y)
                    )
                    x += spacingX
                }
                y += spacingY
            }
        }
    }
}
