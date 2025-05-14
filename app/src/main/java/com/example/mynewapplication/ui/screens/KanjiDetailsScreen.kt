package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.mynewapplication.data.model.Kanji
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiDetailsScreen(
    kanji: String,
    viewModel: KanjiViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val kanjiData = viewModel.getKanjiBySymbol(kanji) ?: Kanji(
        kanji = kanji,
        translation = "Неизвестный иероглиф",
        onReadings = emptyList(),
        kunReadings = emptyList(),
        radical = "",
        strokes = 0,
        grade = "",
        gifPath = null,
        combinations = emptyList()
    )

    val collections by viewModel.collections.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val selectedCollections = remember { mutableStateListOf<Int>() } // Список для хранения выбранных коллекций

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = kanjiData.kanji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Отображаем GIF, если он есть
            kanjiData.gifPath?.let { gifPath ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/$gifPath")
                        .allowHardware(false) // Отключаем аппаратное ускорение
                        .decoderFactory { result, options, _ ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                ImageDecoderDecoder(result.source, options)
                            } else {
                                GifDecoder(result.source, options)
                            }
                        }
                        .build(),
                    contentDescription = "GIF анимация написания иероглифа",
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Перевод: ${kanjiData.translation}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Чтения он: ${if (kanjiData.onReadings.isEmpty()) "Нет" else kanjiData.onReadings.joinToString(", ")}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Чтения кун: ${if (kanjiData.kunReadings.isEmpty()) "Нет" else kanjiData.kunReadings.joinToString(", ")}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Радикал: ${kanjiData.radical}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Черты: ${kanjiData.strokes}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Уровень: ${kanjiData.grade}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Добавить в коллекцию"
            Button(
                onClick = {
                    if (collections.isNotEmpty()) {
                        showDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = collections.isNotEmpty()
            ) {
                Text("Добавить в коллекцию")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Диалог для выбора коллекций
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        selectedCollections.clear()
                    },
                    title = { Text("Выберите коллекции") },
                    text = {
                        LazyColumn {
                            items(collections) { collection ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = collection.id in selectedCollections,
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                selectedCollections.add(collection.id)
                                            } else {
                                                selectedCollections.remove(collection.id)
                                            }
                                        }
                                    )
                                    Text(
                                        text = collection.name,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedCollections.forEach { collectionId ->
                                    val collection = collections.find { it.id == collectionId }
                                    collection?.let {
                                        viewModel.addToCollection(it, kanjiData.kanji)
                                    }
                                }
                                showDialog = false
                                selectedCollections.clear()
                            },
                            enabled = selectedCollections.isNotEmpty()
                        ) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                selectedCollections.clear()
                            }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // Отображаем примеры сочетаний как текст
            if (kanjiData.combinations.isNotEmpty()) {
                Text(
                    text = "Примеры сочетаний:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(kanjiData.combinations) { combination ->
            Text(
                text = "${combination.kanji} (${combination.transcription}) - ${combination.translations.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}