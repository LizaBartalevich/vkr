package com.example.mynewapplication.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mynewapplication.ui.screens.AllKanjiScreen
import com.example.mynewapplication.ui.screens.CollectionsScreen
import com.example.mynewapplication.ui.screens.HomeScreen
import com.example.mynewapplication.ui.screens.KanjiDetailsScreen
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: KanjiViewModel
) {
    // Получаем начальное значение темы в composable-контексте
    val initialDarkTheme = isSystemInDarkTheme()
    // Теперь используем remember с начальным значением
    val isDarkTheme = remember { mutableStateOf(initialDarkTheme) }

    // Список пунктов навигации
    val items = listOf(
        Screen.Camera,
        Screen.AllKanji,
        Screen.Collections
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Camera -> Icons.Default.Camera
                                    Screen.AllKanji -> Icons.Default.List
                                    Screen.Collections -> Icons.Default.CollectionsBookmark
                                },
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Избегаем накопления одинаковых экранов в стеке
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Camera.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Camera.route) {
                HomeScreen(
                    navController = navController,
                    viewModel = viewModel,

                )
            }
            composable(Screen.AllKanji.route) {
                AllKanjiScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(Screen.Collections.route) {
                CollectionsScreen(navController = navController, viewModel = viewModel)
            }
            composable("kanjiDetail/{kanji}") { backStackEntry ->
                val kanji = backStackEntry.arguments?.getString("kanji") ?: ""
                KanjiDetailsScreen(
                    kanji = kanji,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("collection_details/{collectionId}") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId")?.toIntOrNull() ?: 0
                Text("Экран деталей коллекции (заглушка) для ID: $collectionId")
            }
        }
    }
}

// Определяем экраны для нижней панели навигации
sealed class Screen(val route: String, val title: String) {
    object Camera : Screen("home", "Камера")
    object AllKanji : Screen("all_kanji", "Все кандзи")
    object Collections : Screen("collections", "Коллекции")
}