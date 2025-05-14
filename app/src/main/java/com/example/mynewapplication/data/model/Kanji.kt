package com.example.mynewapplication.data.model

import com.google.gson.annotations.SerializedName

data class Kanji(
    @SerializedName("kanji") val kanji: String,
    @SerializedName("translation") val translation: String,
    @SerializedName("onReadings") val onReadings: List<String>,
    @SerializedName("kunReadings") val kunReadings: List<String>,
    @SerializedName("combinations") val combinations: List<Combination>, // Исправлено на List<Combination>
    @SerializedName("radical") val radical: String,
    @SerializedName("strokes") val strokes: Int,
    @SerializedName("grade") val grade: String,
    @SerializedName("gifPath") val gifPath: String?
)

data class Combination(
    @SerializedName("kanji") val kanji: String,
    @SerializedName("transcription") val transcription: String,
    @SerializedName("translations") val translations: List<String>
)