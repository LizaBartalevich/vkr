package com.example.mynewapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kanji_collections")
data class KanjiCollection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val kanjiList: String
)