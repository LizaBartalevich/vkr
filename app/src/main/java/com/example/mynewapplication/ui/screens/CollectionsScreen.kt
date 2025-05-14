package com.example.mynewapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun CollectionsScreen(navController: NavController, viewModel: KanjiViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("name") } // name, count, date
    val collections by viewModel.collections.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    Log.d("CollectionsScreen", "Collections size: ${collections.size}, content: $collections")
    // Сортировка коллекций
    val sortedCollections = when (sortBy) {
        "count" -> collections.sortedByDescending {
            it.kanjiList.split(",").filter { kanji -> kanji.isNotEmpty() }.size
        }
        "date" -> collections.sortedByDescending { it.id }
        else -> collections.sortedBy { it.name }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Коллекции",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchCollections(it)
            },
            label = { Text("Поиск коллекций") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Text(
            text = "Сортировать по:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(listOf("name", "count", "date")) { sortOption ->
                Button(
                    onClick = { sortBy = sortOption },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = when (sortOption) {
                            "name" -> "Названию"
                            "count" -> "Кол-ву кандзи"
                            "date" -> "Дате"
                            else -> sortOption
                        },
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Кнопка создания новой коллекции
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Создать новую коллекцию")
        }

        // Диалог для создания новой коллекции
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateDialog = false
                    newCollectionName = ""
                },
                title = { Text("Создать коллекцию") },
                text = {
                    TextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        label = { Text("Название коллекции") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCollectionName.isNotBlank()) {
                                viewModel.createCollection(newCollectionName, "")
                                showCreateDialog = false
                                newCollectionName = ""
                                searchQuery = "" // Сбрасываем поиск
                            }
                        },
                        enabled = newCollectionName.isNotBlank()
                    ) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showCreateDialog = false
                            newCollectionName = ""
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (sortedCollections.isEmpty()) {
            Text(
                text = "Коллекций пока нет. Добавьте иероглифы в коллекцию через экран информации об иероглифе!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(sortedCollections) { collection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("collection_details/${collection.id}")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = collection.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Для коллекции "Распознанные кандзи" отображаем только уникальные иероглифы
                            val kanjiList = if (collection.name == "Распознанные кандзи") {
                                collection.kanjiList.split(",").distinct().filter { it.isNotEmpty() }
                            } else {
                                collection.kanjiList.split(",").filter { it.isNotEmpty() }
                            }
                            if (kanjiList.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(kanjiList) { kanji ->
                                        Text(
                                            text = kanji,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Иероглифов пока нет",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}