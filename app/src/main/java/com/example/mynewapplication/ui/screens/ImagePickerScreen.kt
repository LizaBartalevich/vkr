package com.example.mynewapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun ImagePickerScreen(navController: NavController, viewModel: KanjiViewModel) {
    val context = LocalContext.current
    var hasStoragePermission by remember { mutableStateOf(false) }
    var shouldShowPermissionRationale by remember { mutableStateOf(false) }

    // Разрешение для доступа к изображениям
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Запрос разрешения
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasStoragePermission = isGranted
        if (!isGranted) {
            shouldShowPermissionRationale = ContextCompat.checkSelfPermission(
                context,
                storagePermission
            ) == PackageManager.PERMISSION_DENIED
        }
    }

    // Запуск выбора изображения через PickVisualMedia
    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
            viewModel.recognizeKanji(bitmap)
        }
    }

    // Проверка разрешения при запуске экрана
    LaunchedEffect(Unit) {
        hasStoragePermission = ContextCompat.checkSelfPermission(
            context,
            storagePermission
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasStoragePermission) {
            permissionLauncher.launch(storagePermission)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (hasStoragePermission) {
            // Проверяем поддержку Selected Photos Access на Android 14+
            val maxImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                MediaStore.getPickImagesMaxLimit()
            } else {
                1 // На старых версиях Android выбираем только одно изображение
            }

            Button(
                onClick = {
                    val request = PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        .build()
                    pickMediaLauncher.launch(request)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать изображение из галереи (максимум: $maxImages)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Отображение распознанных кандзи
            val recognizedKanji by viewModel.recognizedKanji.collectAsState()
            LazyColumn {
                items(recognizedKanji) { kanji ->
                    Text(
                        text = kanji,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("kanji_details/$kanji")
                            }
                            .padding(8.dp)
                    )
                }
            }
        } else {
            Text(
                text = if (shouldShowPermissionRationale) {
                    "Разрешение на доступ к изображениям необходимо для выбора фото. Пожалуйста, предоставьте разрешение в настройках."
                } else {
                    "Требуется разрешение на доступ к изображениям."
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}