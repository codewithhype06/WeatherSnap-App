package com.example.weathersnap.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.weathersnap.utils.ImageUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onImageCaptured: (String, Long, Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Camera Permission Required", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5E1A5))
                ) { Text("Grant Permission", color = Color.Black) }
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    // 2. Camera Setup (Safe Initialization)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    var isCapturing by remember { mutableStateOf(false) }

    // 3. Bind Camera properly
    LaunchedEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 4. UI Rendering
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "Custom Camera",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("Close")
            }

            Button(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            executor = cameraExecutor,
                            onResult = { path, orig, comp ->
                                isCapturing = false
                                onImageCaptured(path, orig, comp)
                            },
                            onError = { isCapturing = false }
                        )
                    }
                },
                enabled = !isCapturing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5E1A5))
            ) {
                // 🚨 FIX: Removed CircularProgressIndicator to avoid animation version crash
                if (isCapturing) {
                    Text("Saving...", color = Color.Black)
                } else {
                    Text("Capture", color = Color.Black)
                }
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

// 5. Optimized Capture Function
// 5. Optimized Capture Function
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onResult: (String, Long, Long) -> Unit,
    onError: () -> Unit
) {
    val photoFile = File(context.cacheDir, "weathersnap_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Compression ka bhaari kaam background mein
                executor.execute {
                    try {
                        val originalSize = ImageUtils.getFileSizeKb(photoFile)
                        val compressedFile = ImageUtils.compressImage(context, photoFile)
                        val compressedSize = ImageUtils.getFileSizeKb(compressedFile)

                        // 🚨 FIX: Navigation (onResult) ko wapas MAIN THREAD par bhej diya!
                        ContextCompat.getMainExecutor(context).execute {
                            onResult(compressedFile.absolutePath, originalSize, compressedSize)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val originalSize = ImageUtils.getFileSizeKb(photoFile)

                        // 🚨 FIX: Error aane par bhi Main Thread se hi wapas bhejo
                        ContextCompat.getMainExecutor(context).execute {
                            onResult(photoFile.absolutePath, originalSize, originalSize)
                        }
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                // onError is already called on main executor because of takePicture param
                onError()
            }
        }
    )
}