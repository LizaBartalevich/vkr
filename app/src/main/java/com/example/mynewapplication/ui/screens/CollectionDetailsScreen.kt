package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(collectionId: Int, navController: NavController, viewModel: KanjiViewModel) {
    val collections by viewModel.collections.collectAsState()
    val collection = collections.find { it.id == collectionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection?.name ?: "Collection Details") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        if (collection == null) {
            Text(
                text = "Collection not found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Kanji in ${collection.name}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val kanjiList = collection.kanjiList.split(",").filter { it.isNotEmpty() }
                if (kanjiList.isEmpty()) {
                    Text(
                        text = "No kanji in this collection yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(kanjiList) { kanji ->
                            Card(
                                modifier = Modifier
                                    .clickable { navController.navigate("kanjiDetail/$kanji") }
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = kanji,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}