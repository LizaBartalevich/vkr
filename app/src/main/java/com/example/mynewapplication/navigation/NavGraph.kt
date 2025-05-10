package com.example.mynewapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mynewapplication.ui.screens.CameraScreen
import com.example.mynewapplication.ui.screens.ImagePickerScreen
import com.example.mynewapplication.ui.screens.KanjiDetailsScreen
import com.example.mynewapplication.ui.screens.KanjiListScreen
import com.example.mynewapplication.ui.screens.CollectionsScreen
import com.example.mynewapplication.ui.screens.CollectionDetailsScreen
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: KanjiViewModel = viewModel()

    NavHost(navController = navController, startDestination = "kanji_list") {
        composable("kanji_list") {
            KanjiListScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = "kanji_details/{kanji}",
            arguments = listOf(navArgument("kanji") { type = NavType.StringType })
        ) { backStackEntry ->
            val kanji = backStackEntry.arguments?.getString("kanji") ?: ""
            KanjiDetailsScreen(kanji = kanji, navController = navController, viewModel = viewModel)
        }
        composable("camera") {
            CameraScreen(navController = navController, viewModel = viewModel)
        }
        composable("image_picker") {
            ImagePickerScreen(navController = navController, viewModel = viewModel)
        }
        composable("collections") {
            CollectionsScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = "collection_details/{collectionId}",
            arguments = listOf(navArgument("collectionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: 0
            CollectionDetailsScreen(
                collectionId = collectionId,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}