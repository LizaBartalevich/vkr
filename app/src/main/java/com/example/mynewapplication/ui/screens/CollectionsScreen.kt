package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun CollectionsScreen(navController: NavController, viewModel: KanjiViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("name") } // name, count, date
    val collections by viewModel.collections.collectAsState()

    // Сортировка коллекций
    val sortedCollections = when (sortBy) {
        "count" -> collections.sortedByDescending { it.kanjiList.split(",").size }
        "date" -> collections.sortedByDescending { it.id }
        else -> collections.sortedBy { it.name }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Collections",
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
            label = { Text("Search Collections") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sort by:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            listOf("name", "count", "date").forEach { sortOption ->
                Button(
                    onClick = { sortBy = sortOption },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(sortOption.replaceFirstChar { it.uppercase() })
                }
            }
        }
        if (sortedCollections.isEmpty()) {
            Text(
                text = "No collections yet. Add kanji to a collection from the details screen!",
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Kanji: ${collection.kanjiList}",
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