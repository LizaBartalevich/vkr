package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllKanjiScreen(navController: NavController, viewModel: KanjiViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var activeSearchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("strokes") } // strokes, grade
    var filterByGrades by remember { mutableStateOf<Set<String>>(emptySet()) } // фильтр по уровням (grade)
    var filterByStrokes by remember { mutableStateOf<Set<Int>>(emptySet()) } // фильтр по количеству черт

    // Загружаем список иероглифов из viewModel
    val allKanji by remember { mutableStateOf(viewModel.getAllKanji()) }

    // Фильтрация, сортировка и поиск
    val filteredKanji = allKanji
        .filter { kanji ->
            if (activeSearchQuery.isEmpty()) true
            else kanji.kanji.contains(activeSearchQuery, ignoreCase = true) ||
                    kanji.translation.contains(activeSearchQuery, ignoreCase = true) ||
                    kanji.onReadings.any { it.contains(activeSearchQuery, ignoreCase = true) } ||
                    kanji.kunReadings.any { it.contains(activeSearchQuery, ignoreCase = true) } ||
                    kanji.combinations.any { combination ->
                        combination.kanji.contains(activeSearchQuery, ignoreCase = true) ||
                                combination.transcription.contains(activeSearchQuery, ignoreCase = true) ||
                                combination.translations.any { it.contains(activeSearchQuery, ignoreCase = true) }
                    }
        }
        .filter { kanji ->
            if (filterByGrades.isEmpty()) true
            else filterByGrades.contains(kanji.grade)
        }
        .filter { kanji ->
            if (filterByStrokes.isEmpty()) true
            else filterByStrokes.contains(kanji.strokes)
        }
        .sortedWith(
            when (sortBy) {
                "strokes" -> compareBy { it.strokes }
                "grade" -> compareBy { it.grade }
                else -> compareBy { it.kanji }
            }
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все иероглифы") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Поле поиска с кнопкой
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск (иероглиф, перевод, чтение, комбинации)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { activeSearchQuery = searchQuery }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }
                }
            )

            // Фильтр по уровню
            Text(
                text = "Фильтр по уровню:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Уникальные значения grade из списка
                val grades = allKanji.map { it.grade }.distinct().sorted()
                item {
                    FilterButton(
                        text = "Все",
                        isSelected = filterByGrades.isEmpty(),
                        onClick = { filterByGrades = emptySet() }
                    )
                }
                items(grades) { grade ->
                    FilterButton(
                        text = grade,
                        isSelected = filterByGrades.contains(grade),
                        onClick = {
                            filterByGrades = if (filterByGrades.contains(grade)) {
                                filterByGrades - grade
                            } else {
                                filterByGrades + grade
                            }
                        }
                    )
                }
            }

            // Фильтр по количеству черт
            Text(
                text = "Фильтр по количеству черт:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Уникальные значения strokes из списка
                val strokes = allKanji.map { it.strokes }.distinct().sorted()
                item {
                    FilterButton(
                        text = "Все",
                        isSelected = filterByStrokes.isEmpty(),
                        onClick = { filterByStrokes = emptySet() }
                    )
                }
                items(strokes) { stroke ->
                    FilterButton(
                        text = stroke.toString(),
                        isSelected = filterByStrokes.contains(stroke),
                        onClick = {
                            filterByStrokes = if (filterByStrokes.contains(stroke)) {
                                filterByStrokes - stroke
                            } else {
                                filterByStrokes + stroke
                            }
                        }
                    )
                }
            }

            // Сортировка
            Text(
                text = "Сортировать по:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listOf("strokes", "grade")) { sortOption ->
                    Button(
                        onClick = { sortBy = sortOption },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sortBy == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            when (sortOption) {
                                "strokes" -> "Чертам"
                                "grade" -> "Уровню"
                                else -> sortOption
                            }
                        )
                    }
                }
            }

            // Список иероглифов
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredKanji) { kanji ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("kanjiDetail/${kanji.kanji}") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = kanji.kanji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "Черты: ${kanji.strokes}, Уровень: ${kanji.grade}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text)
    }
}