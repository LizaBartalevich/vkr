package com.example.mynewapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.mynewapplication.navigation.NavGraph
import com.example.mynewapplication.ui.theme.MyNewApplicationTheme
import com.example.mynewapplication.ui.viewmodel.KanjiViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNewApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavGraph()
                }
            }
        }
//        val viewModel: KanjiViewModel = viewModel()
//        viewModel.testInsertCollection()
    }
}