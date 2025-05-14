package com.example.mynewapplication.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.graphics.scale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: KanjiViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Разрешение на доступ к камере
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Разрешение на доступ к галерее
    val storagePermissionState = rememberPermissionState(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    // Состояние для отображения ошибки доступа к камере
    var showCameraError by remember { mutableStateOf(false) }

    // Состояние для отображения ошибки доступа к галерее
    var showStorageError by remember { mutableStateOf(false) }

    // Флаг для отслеживания попытки распознавания
    var hasAttemptedRecognition by remember { mutableStateOf(false) }

    // Состояние для захваченного изображения
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Executor для камеры
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Состояние распознавания иероглифов
    val recognizedKanji by viewModel.recognizedKanji.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()

    // Лаунчер для выбора изображения из галереи
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    // Масштабируем изображение перед передачей
                    val scaledBitmap = scaleBitmap(bitmap, 1000, 750)
                    hasAttemptedRecognition = true
                    viewModel.recognizeKanji(scaledBitmap)
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        if (!storagePermissionState.status.isGranted) {
            storagePermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(recognizedKanji) {
        if (!isRecognizing && recognizedKanji.isNotEmpty()) {
            // Переходим на страницу первого распознанного иероглифа
            navController.navigate("kanjiDetail/${recognizedKanji.first()}")
            capturedBitmap = null // Сбрасываем захваченное изображение после перехода
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cameraPermissionState.status.isGranted) {
            if (capturedBitmap == null) {
                // Показываем предпросмотр камеры, если изображение не захвачено
                CameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    cameraExecutor = cameraExecutor,
                    modifier = Modifier.fillMaxSize(),
                    onImageCaptured = { bitmap ->
                        Log.d("HomeScreen", "Image captured successfully")
                        capturedBitmap = bitmap // Сохраняем захваченное изображение
                        hasAttemptedRecognition = true
                        viewModel.recognizeKanji(bitmap)
                    },
                    onGalleryClick = {
                        if (storagePermissionState.status.isGranted) {
                            galleryLauncher.launch("image/*")
                        } else {
                            storagePermissionState.launchPermissionRequest()
                            if (!storagePermissionState.status.isGranted) {
                                showStorageError = true
                            }
                        }
                    }
                )
            } else {
                // Показываем захваченное изображение
                Image(
                    painter = rememberAsyncImagePainter(capturedBitmap),
                    contentDescription = "Захваченное изображение",
                    modifier = Modifier.fillMaxSize()
                )

                // Кнопка для нового захвата
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            Log.d("HomeScreen", "Capture button clicked")
                            capturedBitmap = null
                            hasAttemptedRecognition = false
                        }
                    ) {
                        Text("Сделать фото")
                    }
                }
            }

            when {
                isRecognizing -> {
                    // Показываем индикатор загрузки, пока идёт распознавание
                    CircularProgressIndicator()
                }
                hasAttemptedRecognition && recognizedKanji.isEmpty() -> {
                    // Показываем сообщение только после попытки распознавания
                    Text(
                        text = "Иероглифы не распознаны",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        } else {
            if (showCameraError) {
                Text(
                    text = "Нет доступа к камере. Пожалуйста, предоставьте разрешение.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Button(
                    onClick = {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        if (!cameraPermissionState.status.isGranted) {
                            showCameraError = true
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("Запросить доступ к камере")
                }
            }
        }

        if (showStorageError) {
            Text(
                text = "Нет доступа к галерее. Пожалуйста, предоставьте разрешение.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    // Вычисляем коэффициент масштабирования
    val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

    // Если масштабирование не требуется, возвращаем исходный битмап
    if (scale >= 1) return bitmap

    val newWidth = (width * scale).toInt()
    val newHeight = (height * scale).toInt()

    return bitmap.scale(newWidth, newHeight)
}