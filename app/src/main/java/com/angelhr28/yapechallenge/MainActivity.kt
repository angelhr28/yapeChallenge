package com.angelhr28.yapechallenge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.angelhr28.yapechallenge.core.ui.theme.YapeChallengeTheme
import com.angelhr28.yapechallenge.feature.documents.DocumentsIntent
import com.angelhr28.yapechallenge.feature.documents.DocumentsViewModel
import com.angelhr28.yapechallenge.navigation.YapeChallengeNavGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

class MainActivity : FragmentActivity() {

    private var showCamera by mutableStateOf(false)
    private var pendingSnackbar by mutableStateOf<String?>(null)
    private var imageCapture: ImageCapture? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showCamera = true
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            fetchLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocationPermission()

        setContent {
            YapeChallengeTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Show pending snackbar from camera capture
                val message = pendingSnackbar
                if (message != null && !showCamera) {
                    pendingSnackbar = null
                    scope.launch {
                        val job = launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Indefinite) }
                        delay(2000)
                        job.cancel()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    if (showCamera) {
                        val documentsViewModel: DocumentsViewModel = koinViewModel()
                        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                            AndroidView(
                                factory = { ctx ->
                                    PreviewView(ctx).also { previewView ->
                                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                        cameraProviderFuture.addListener({
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.surfaceProvider = previewView.surfaceProvider
                                            }
                                            imageCapture = ImageCapture.Builder()
                                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                                .build()
                                            try {
                                                cameraProvider.unbindAll()
                                                cameraProvider.bindToLifecycle(
                                                    this@MainActivity,
                                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                                    preview,
                                                    imageCapture
                                                )
                                            } catch (e: Exception) {
                                                Log.e("YapeChallenge", "Camera bind failed", e)
                                            }
                                        }, ContextCompat.getMainExecutor(ctx))
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            FloatingActionButton(
                                onClick = { capturePhoto(documentsViewModel) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = "Capturar")
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        YapeChallengeNavGraph(
                            navController = navController,
                            onTakePhoto = {
                                if (ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    showCamera = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun capturePhoto(viewModel: DocumentsViewModel) {
        val imageCapture = imageCapture ?: return
        val photoFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bytes = photoFile.readBytes()
                    viewModel.processIntent(
                        DocumentsIntent.AddDocument(
                            name = "foto_${System.currentTimeMillis()}.jpg",
                            mimeType = "image/jpeg",
                            bytes = bytes
                        )
                    )
                    photoFile.delete()
                    pendingSnackbar = "Documento agregado exitosamente"
                    showCamera = false
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("YapeChallenge", "Photo capture failed", exception)
                    showCamera = false
                }
            }
        )
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun fetchLocation() {
        // Location is now fetched directly in DetailScreen for watermark
        // This method only validates permissions were granted
    }
}
