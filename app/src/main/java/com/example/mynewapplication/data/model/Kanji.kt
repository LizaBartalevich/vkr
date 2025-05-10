package com.example.mynewapplication.data.model

data class Kanji(
    val kanji: String,
    val translation: String,
    val onReadings: List<String>,
    val kunReadings: List<String>,
    val radical: String,
    val strokes: Int,
    val grade: Int,
    val gifPath: String? = null,
    val combinations: List<String> = emptyList()
)
