package com.example.mynewapplication.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.OrientationEventListener
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    modifier: Modifier = Modifier.fillMaxSize(),
    onImageCaptured: (Bitmap) -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Состояние для хранения текущей ориентации устройства
    var deviceOrientation by remember { mutableIntStateOf(0) }

    // OrientationEventListener для отслеживания ориентации устройства
    val orientationListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                deviceOrientation = when {
                    orientation < 45 || orientation >= 315 -> 0    // Портретная (0°)
                    orientation < 135 -> 90                        // Ландшафтная (90°)
                    orientation < 225 -> 180                       // Портретная перевёрнутая (180°)
                    else -> 270                                    // Ландшафтная перевёрнутая (270°)
                }
            }
        }
    }

    // Активируем OrientationEventListener
    DisposableEffect(Unit) {
        orientationListener.enable()
        onDispose {
            orientationListener.disable()
        }
    }

    Box(
        modifier = modifier
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error binding camera: ${e.message}", e)
                }

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Линейка кнопок внизу
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Расстояние между кнопками 8.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круглая кнопка выбора из галереи
            IconButton(
                onClick = {
                    Log.d("CameraPreview", "Gallery button clicked")
                    onGalleryClick()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Выбрать из галереи",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Кнопка захвата изображения
            Button(
                onClick = {
                    Log.d("CameraPreview", "Capture button clicked")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        context.contentResolver,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        android.content.ContentValues()
                    ).build()

                    imageCapture.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                Log.d("CameraPreview", "Image saved, URI: ${output.savedUri}")
                                output.savedUri?.let { uri ->
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    if (inputStream != null) {
                                        val bitmap = BitmapFactory.decodeStream(inputStream)
                                        inputStream.close()
                                        if (bitmap != null) {
                                            // Поворачиваем изображение в зависимости от ориентации устройства
                                            val matrix = Matrix()
                                            matrix.postRotate(deviceOrientation.toFloat())
                                            val rotatedBitmap = Bitmap.createBitmap(
                                                bitmap,
                                                0,
                                                0,
                                                bitmap.width,
                                                bitmap.height,
                                                matrix,
                                                true
                                            )
                                            bitmap.recycle() // Освобождаем исходный bitmap
                                            Log.d("CameraPreview", "Bitmap decoded and rotated successfully")
                                            onImageCaptured(rotatedBitmap)
                                        } else {
                                            Log.e("CameraPreview", "Failed to decode bitmap from URI: $uri")
                                        }
                                    } else {
                                        Log.e("CameraPreview", "Failed to open input stream for URI: $uri")
                                    }
                                } ?: run {
                                    Log.e("CameraPreview", "Saved URI is null")
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Error capturing image: ${exception.message}", exception)
                            }
                        }
                    )
                }
            ) {
                Text("Сделать фото")
            }
        }
    }
}