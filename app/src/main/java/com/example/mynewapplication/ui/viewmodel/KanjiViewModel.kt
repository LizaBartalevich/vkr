package com.example.mynewapplication.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynewapplication.data.db.AppDatabase
import com.example.mynewapplication.data.model.KanjiCollection
import com.example.mynewapplication.data.repository.KanjiCollectionRepository
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class KanjiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KanjiCollectionRepository
    private val _collections = MutableStateFlow<List<KanjiCollection>>(emptyList())
    val collections = _collections.asStateFlow()

    private val _recognizedKanji = MutableStateFlow<List<String>>(emptyList())
    val recognizedKanji = _recognizedKanji.asStateFlow()

    private var tessBaseAPI: TessBaseAPI? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = KanjiCollectionRepository(db.kanjiCollectionDao())
        initializeTesseract(application)
        fetchCollections()
    }

    private fun initializeTesseract(context: Context) {
        try {
            tessBaseAPI = TessBaseAPI()
            val dataPath = File(context.filesDir, "tesseract").absolutePath
            val tessDataPath = File(dataPath, "tessdata").absolutePath

            // Копируем tessdata из assets в filesDir
            val tessDataDir = File(tessDataPath)
            val trainedDataFile = File(tessDataPath, "jpn_finetuned.traineddata")
            if (!trainedDataFile.exists()) {
                tessDataDir.mkdirs()
                val assetManager = context.assets
                val inputStream = assetManager.open("tessdata/jpn_finetuned.traineddata")
                val outputStream = FileOutputStream(trainedDataFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                Log.d("KanjiViewModel", "Tessdata copied successfully")
            } else {
                Log.d("KanjiViewModel", "Tessdata already exists")
            }

            // Инициализируем Tesseract с jpn_finetuned
            val initialized = tessBaseAPI!!.init(dataPath, "jpn")
            if (!initialized) {
                Log.e("KanjiViewModel", "Could not initialize Tesseract")
                tessBaseAPI = null
            } else {
                Log.d("KanjiViewModel", "Tesseract initialized successfully")
            }
        } catch (e: IOException) {
            Log.e("KanjiViewModel", "Error initializing Tesseract", e)
            tessBaseAPI = null
        }
    }

    fun recognizeKanji(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (tessBaseAPI == null) {
                    Log.e("KanjiViewModel", "Tesseract is not initialized")
                    _recognizedKanji.value = emptyList()
                    return@launch
                }

                tessBaseAPI!!.setImage(bitmap)
                val recognizedText = tessBaseAPI!!.utF8Text
                Log.d("KanjiViewModel", "Raw recognized text: $recognizedText")

                val kanjiList = recognizedText
                    .replace("\n", "")
                    .replace(" ", "")
                    .split("")
                    .filter { char -> char.isNotEmpty() && char.matches("\\p{InCJKUnifiedIdeographs}".toRegex()) }

                Log.d("KanjiViewModel", "Filtered Kanji: $kanjiList")
                _recognizedKanji.value = kanjiList
            } catch (e: Exception) {
                Log.e("KanjiViewModel", "Error recognizing Kanji", e)
                _recognizedKanji.value = emptyList()
            }
        }
    }

    private fun fetchCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { collections: List<KanjiCollection> ->
                _collections.value = collections
            }
        }
    }

    fun searchCollections(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                fetchCollections()
            } else {
                repository.searchCollections(query).collect { collections: List<KanjiCollection> ->
                    _collections.value = collections
                }
            }
        }
    }

    fun addToCollection(collection: KanjiCollection, kanji: String) {
        viewModelScope.launch {
            val updatedKanjiList = collection.kanjiList.split(",").toMutableList()
            if (kanji !in updatedKanjiList) {
                updatedKanjiList.add(kanji)
                val updatedCollection = collection.copy(kanjiList = updatedKanjiList.joinToString(","))
                repository.updateCollection(updatedCollection)
            }
        }
    }

    fun removeFromCollection(collection: KanjiCollection, kanji: String) {
        viewModelScope.launch {
            val updatedKanjiList = collection.kanjiList.split(",").toMutableList()
            updatedKanjiList.remove(kanji)
            val updatedCollection = collection.copy(kanjiList = updatedKanjiList.joinToString(","))
            repository.updateCollection(updatedCollection)
        }
    }

    fun createCollection(name: String, kanji: String) {
        viewModelScope.launch {
            val newCollection = KanjiCollection(name = name, kanjiList = kanji)
            repository.insertCollection(newCollection)
        }
    }

    fun renameCollection(collection: KanjiCollection, newName: String) {
        viewModelScope.launch {
            val updatedCollection = collection.copy(name = newName)
            repository.updateCollection(updatedCollection)
        }
    }

    fun deleteCollection(collection: KanjiCollection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }

    override fun onCleared() {
        tessBaseAPI?.end()
        super.onCleared()
    }

    fun testInsertCollection() {
        viewModelScope.launch {
            val testCollection = KanjiCollection(name = "Test Collection", kanjiList = "日,月")
            repository.insertCollection(testCollection)
            Log.d("KanjiViewModel", "Test collection inserted")
        }
    }
}