package com.example.mynewapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mynewapplication.data.model.Kanji
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun KanjiDetailsScreen(
    kanji: String,
    viewModel: KanjiViewModel,
    navController: NavController
) {
    // Здесь мы должны получить данные о конкретном иероглифе
    // Для примера, предположим, что у нас есть список иероглифов
    // В реальном приложении это может быть запрос к API или базе данных
    val dummyKanji = Kanji(
        kanji = kanji,
        translation = "Пример перевода",
        onReadings = listOf("オン"),
        kunReadings = listOf("くん"),
        radical = "⺈",
        strokes = 3,
        grade = 1,
        gifPath = null,
        combinations = listOf("組合", "結合")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dummyKanji.kanji,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Перевод: ${dummyKanji.translation}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Чтения он: ${dummyKanji.onReadings.joinToString(", ")}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Чтения кун: ${dummyKanji.kunReadings.joinToString(", ")}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Радикал: ${dummyKanji.radical}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Штрихи: ${dummyKanji.strokes}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Уровень: ${dummyKanji.grade}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        dummyKanji.combinations.forEach { combination ->
            Button(
                onClick = {
                    navController.navigate("kanji_details/$combination")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = combination)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        val collections = viewModel.collections.collectAsState().value
        collections.forEach { collection ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = collection.name)
                Button(
                    onClick = {
                        viewModel.addToCollection(collection, dummyKanji.kanji)
                    }
                ) {
                    Text("Добавить в коллекцию")
                }
            }
        }
    }
}