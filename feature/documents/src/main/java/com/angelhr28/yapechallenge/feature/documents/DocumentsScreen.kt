package com.angelhr28.yapechallenge.feature.documents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.angelhr28.yapechallenge.core.extensions.toFormattedDate
import com.angelhr28.yapechallenge.core.extensions.toShortDate
import com.angelhr28.yapechallenge.core.ui.components.YapeChallengeTopBar
import com.angelhr28.yapechallenge.core.ui.components.EmptyState
import com.angelhr28.yapechallenge.core.ui.components.FilterChipRow
import com.angelhr28.yapechallenge.core.ui.components.FilterOption
import com.angelhr28.yapechallenge.core.ui.theme.ImageColor
import com.angelhr28.yapechallenge.core.ui.theme.PdfColor
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla principal de listado de documentos.
 *
 * Muestra los documentos almacenados con filtros por tipo, permite agregar documentos
 * desde la galeria o camara, y navega al detalle al seleccionar un documento.
 *
 * @param onNavigateToDetail Callback invocado con el ID del documento para navegar al detalle.
 * @param onTakePhoto Callback para abrir la camara y capturar una foto.
 * @param viewModel ViewModel que gestiona el estado de la pantalla.
 */
@Composable
fun DocumentsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onTakePhoto: () -> Unit,
    viewModel: DocumentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAddMenu by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val bytes = contentResolver.openInputStream(uri)?.readBytes()
            val displayName = uri.lastPathSegment ?: "documento_${System.currentTimeMillis()}"
            if (bytes != null) {
                viewModel.processIntent(
                    DocumentsIntent.AddDocument(
                        name = displayName,
                        mimeType = mimeType,
                        bytes = bytes
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocumentsEffect.NavigateToDetail -> onNavigateToDetail(effect.documentId)
                is DocumentsEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is DocumentsEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            YapeChallengeTopBar(title = "YapeChallenge")
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showAddMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar documento"
                    )
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Desde galería") },
                        onClick = {
                            showAddMenu = false
                            galleryLauncher.launch(
                                arrayOf("image/*", "application/pdf")
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tomar foto") },
                        onClick = {
                            showAddMenu = false
                            onTakePhoto()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterChipRow(
                filters = listOf(
                    FilterOption("Todos", "ALL", state.selectedFilter == null),
                    FilterOption("PDF", "PDF", state.selectedFilter == DocumentType.PDF),
                    FilterOption("Imágenes", "IMAGE", state.selectedFilter == DocumentType.IMAGE)
                ),
                onFilterSelected = { key ->
                    val filter = when (key) {
                        "PDF" -> DocumentType.PDF
                        "IMAGE" -> DocumentType.IMAGE
                        else -> null
                    }
                    viewModel.processIntent(DocumentsIntent.FilterByType(filter))
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AnimatedVisibility(
                visible = !state.isLoading && state.documents.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "Sin documentos",
                    subtitle = "Agrega tu primer documento usando el botón +"
                )
            }

            AnimatedVisibility(
                visible = !state.isLoading && state.documents.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    items(
                        items = state.documents,
                        key = { it.id }
                    ) { document ->
                        DocumentItem(
                            document = document,
                            onClick = {
                                viewModel.processIntent(DocumentsIntent.OpenDocument(document.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Elemento de la lista que representa un documento con su icono, nombre, tipo, peso y fecha.
 *
 * @param document Documento a mostrar.
 * @param onClick Callback al presionar el elemento.
 * @param modifier Modificador aplicado a la tarjeta.
 */
@Composable
private fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (document.type) {
                            DocumentType.PDF -> PdfColor.copy(alpha = 0.1f)
                            DocumentType.IMAGE -> ImageColor.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.type) {
                        DocumentType.PDF -> Icons.Default.PictureAsPdf
                        DocumentType.IMAGE -> Icons.Default.Image
                    },
                    contentDescription = document.type.displayName,
                    tint = when (document.type) {
                        DocumentType.PDF -> PdfColor
                        DocumentType.IMAGE -> ImageColor
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = document.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = formatFileSize(document.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = document.createdAt.toShortDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Formatea el tamano de archivo en bytes a una representacion legible (B, KB, MB). */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
