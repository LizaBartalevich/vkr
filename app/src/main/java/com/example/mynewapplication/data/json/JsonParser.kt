package com.example.mynewapplication.data.json

import android.content.Context
import com.example.mynewapplication.data.model.Kanji
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader

object JsonParser {
    fun loadKanjiList(context: Context): List<Kanji> {
        val jsonString = context.assets.open("kanji_database.json").bufferedReader().use(BufferedReader::readText)
        val type = object : TypeToken<List<Kanji>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}