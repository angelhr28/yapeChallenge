package com.angelhr28.yapechallenge.feature.detail

import android.graphics.BitmapFactory
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.angelhr28.yapechallenge.core.security.BiometricHelper
import com.angelhr28.yapechallenge.core.ui.components.YapeChallengeTopBar
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.feature.detail.components.AccessLogSection
import com.angelhr28.yapechallenge.feature.detail.components.WatermarkOverlay
import com.angelhr28.yapechallenge.feature.detail.components.ZoomableImage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailScreen(
    documentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val biometricHelper = remember { BiometricHelper() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Prevent screenshots - FLAG_SECURE
    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    LaunchedEffect(documentId) {
        viewModel.processIntent(DetailIntent.LoadDocument(documentId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailEffect.RequestBiometricAuth -> {
                    if (activity != null) {
                        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity
                        if (fragmentActivity != null) {
                            biometricHelper.authenticate(
                                activity = fragmentActivity,
                                title = "Verificación requerida",
                                subtitle = "Autentícate para ver el documento",
                                onSuccess = {
                                    viewModel.processIntent(DetailIntent.OnAuthenticated)
                                },
                                onError = { error ->
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                }
                is DetailEffect.RequestDeleteBiometricAuth -> {
                    if (activity != null) {
                        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity
                        if (fragmentActivity != null) {
                            biometricHelper.authenticate(
                                activity = fragmentActivity,
                                title = "Confirmar eliminación",
                                subtitle = "Autentícate para eliminar el documento",
                                onSuccess = {
                                    viewModel.processIntent(DetailIntent.ConfirmDelete)
                                },
                                onError = { }
                            )
                        }
                    }
                }
                is DetailEffect.NavigateBack -> onNavigateBack()
                is DetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is DetailEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar documento") },
            text = { Text("¿Estás seguro de que deseas eliminar este documento? Esta acción requiere autenticación biométrica y no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.processIntent(DetailIntent.RequestDelete)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            YapeChallengeTopBar(
                title = state.document?.name ?: "Detalle",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    if (state.isAuthenticated) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar documento",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                !state.isAuthenticated -> {
                    Text(
                        text = "Autenticación requerida para ver este documento",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                state.isAuthenticated && state.document != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Document viewer with watermark
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            when (state.document?.type) {
                                DocumentType.IMAGE -> {
                                    state.decryptedBytes?.let { bytes ->
                                        val bitmap = remember(bytes) {
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        }
                                        if (bitmap != null) {
                                            ZoomableImage {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = state.document?.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                    }
                                }
                                DocumentType.PDF -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Documento PDF\n${state.document?.name}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                null -> {}
                            }

                            // Watermark overlay
                            WatermarkOverlay(
                                locationText = state.currentLocation ?: "Ubicación no disponible"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Document info
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = state.document?.name ?: "",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tipo: ${state.document?.type?.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Access logs
                        AccessLogSection(accessLogs = state.accessLogs)

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
