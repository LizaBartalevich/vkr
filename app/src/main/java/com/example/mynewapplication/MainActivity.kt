package com.example.mynewapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.mynewapplication.ui.theme.MyNewApplicationTheme
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel
import com.example.mynewapplication.navigation.NavGraph

class MainActivity : ComponentActivity() {
    private val kanjiViewModel: KanjiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Отключаем полноэкранный режим
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Делаем панель навигации видимой
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())

        setContent {
            MyNewApplicationTheme(isDarkTheme = isSystemInDarkTheme()) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    viewModel = kanjiViewModel
                )
            }
        }
    }
}