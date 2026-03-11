package com.angelhr28.yapechallenge.feature.detail.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.angelhr28.yapechallenge.core.ui.theme.YapeChallengeTheme
import java.io.File

/**
 * Visor de documentos PDF que renderiza cada pagina como bitmap.
 *
 * Utiliza [PdfRenderer] para convertir los bytes del PDF en imagenes visualizables
 * dentro de un contenedor con zoom.
 *
 * @param pdfBytes Bytes del archivo PDF a renderizar.
 * @param modifier Modificador aplicado al componente.
 */
@Composable
fun PdfViewer(
    pdfBytes: ByteArray,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val pdfPages = remember(pdfBytes) {
        renderPdfPages(context.cacheDir, pdfBytes)
    }

    DisposableEffect(pdfPages) {
        onDispose {
            pdfPages.forEach { it.recycle() }
        }
    }

    if (pdfPages.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No se pudo renderizar el PDF",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        ZoomableImage(modifier = modifier.background(YapeChallengeTheme.extendedColors.pdfViewerBackground)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pdfPages.forEachIndexed { index, bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Página ${index + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}

/**
 * Renderiza todas las paginas de un PDF como una lista de [Bitmap].
 *
 * @param cacheDir Directorio de cache donde se almacena el archivo temporal.
 * @param pdfBytes Bytes del archivo PDF.
 * @return Lista de bitmaps, uno por pagina del PDF.
 */
private fun renderPdfPages(cacheDir: File, pdfBytes: ByteArray): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    var tempFile: File? = null
    try {
        tempFile = File(cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
        tempFile.writeBytes(pdfBytes)

        val fileDescriptor = ParcelFileDescriptor.open(
            tempFile, ParcelFileDescriptor.MODE_READ_ONLY
        )
        val renderer = PdfRenderer(fileDescriptor)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(
                page.width * 2,
                page.height * 2,
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmaps.add(bitmap)
        }

        renderer.close()
        fileDescriptor.close()
    } catch (_: Exception) {
    } finally {
        tempFile?.delete()
    }
    return bitmaps
}