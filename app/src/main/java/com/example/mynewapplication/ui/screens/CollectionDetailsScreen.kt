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

@Composable
fun CollectionDetailsScreen(collectionId: Int, navController: NavController, viewModel: KanjiViewModel) {
    val collections by viewModel.collections.collectAsState()
    val collection = collections.find { coll -> coll.id == collectionId }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var searchKanjiQuery by remember { mutableStateOf("") }

    if (collection != null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Rename")
                }
                Button(
                    onClick = {
                        viewModel.deleteCollection(collection)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Delete")
                }
            }
            TextField(
                value = searchKanjiQuery,
                onValueChange = { searchKanjiQuery = it },
                label = { Text("Search Kanji in Collection") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Kanji in Collection:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val filteredKanji = collection.kanjiList.split(",").filter { kanji ->
                kanji.contains(searchKanjiQuery, ignoreCase = true)
            }
            LazyColumn {
                items(filteredKanji) { kanji: String ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = kanji,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    navController.navigate("kanji_details/$kanji")
                                }
                                .padding(8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.removeFromCollection(collection, kanji)
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }

        // Диалог для переименования коллекции
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Collection") },
                text = {
                    TextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        label = { Text("New Collection Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCollectionName.isNotBlank()) {
                                viewModel.renameCollection(collection, newCollectionName)
                                showRenameDialog = false
                                newCollectionName = ""
                            }
                        }
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    Button(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } else {
        Text(
            text = "Collection not found",
            modifier = Modifier.padding(16.dp)
        )
    }
}