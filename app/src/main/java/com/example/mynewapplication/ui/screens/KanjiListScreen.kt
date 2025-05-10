package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiListScreen(viewModel: KanjiViewModel, navController: NavController) {
    val collections by viewModel.collections.collectAsState()
    var selectedCollection by remember { mutableStateOf(collections.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) } // Добавляем состояние для раскрытия меню

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdown для выбора коллекции
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedCollection?.name ?: "Выберите коллекцию",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                collections.forEach { collection ->
                    DropdownMenuItem(
                        text = { Text(collection.name) },
                        onClick = {
                            selectedCollection = collection
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedCollection?.let { collection ->
            val kanjiList = collection.kanjiList.split(",").filter { it.isNotEmpty() }
            LazyColumn {
                items(kanjiList) { kanji ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("kanji_details/$kanji")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = kanji)
                            Button(
                                onClick = {
                                    viewModel.removeFromCollection(collection, kanji)
                                }
                            ) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
    }
}