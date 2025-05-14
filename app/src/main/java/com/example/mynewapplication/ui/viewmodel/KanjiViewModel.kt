package com.example.mynewapplication.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynewapplication.data.db.AppDatabase
import com.example.mynewapplication.data.model.Kanji
import com.example.mynewapplication.data.model.KanjiCollection
import com.example.mynewapplication.data.repository.KanjiCollectionRepository
import com.googlecode.tesseract.android.TessBaseAPI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class KanjiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KanjiCollectionRepository
    private val _collections = MutableStateFlow<List<KanjiCollection>>(emptyList())
    val collections = _collections.asStateFlow()

    private val _recognizedKanji = MutableStateFlow<List<String>>(emptyList())
    val recognizedKanji = _recognizedKanji.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing = _isRecognizing.asStateFlow()

    private var tessBaseAPI: TessBaseAPI? = null
    private var allKanji: List<Kanji> = emptyList()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = KanjiCollectionRepository(db.kanjiCollectionDao())
        initializeTesseract(application)
        fetchCollections()
        loadKanjiData(application)
        createCapturedKanjiCollectionIfNotExists() // Создаём пустую коллекцию "Распознанные кандзи"
    }

    private fun createCapturedKanjiCollectionIfNotExists() {
        viewModelScope.launch {
            val existingCollection = collections.value.find { it.name == "Распознанные кандзи" }
            if (existingCollection == null) {
                val newCollection = KanjiCollection(name = "Распознанные кандзи", kanjiList = "")
                repository.insertCollection(newCollection)
                Log.d("KanjiViewModel", "Created empty Распознанные кандзи collection")
            }
        }
    }

    private fun initializeTesseract(context: Context) {
        try {
            tessBaseAPI = TessBaseAPI()
            val dataPath = File(context.filesDir, "tesseract").absolutePath
            val tessDataPath = File(dataPath, "tessdata").absolutePath

            // Копируем tessdata из assets в filesDir
            val tessDataDir = File(tessDataPath)
            val trainedDataFile = File(tessDataPath, "jpn.traineddata")
            if (!tessDataDir.exists()) {
                tessDataDir.mkdirs()
                Log.d("KanjiViewModel", "Created tessdata directory: $tessDataPath")
            }
            if (!trainedDataFile.exists()) {
                val assetManager = context.assets
                try {
                    val inputStream = assetManager.open("tessdata/jpn.traineddata")
                    val outputStream = FileOutputStream(trainedDataFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    Log.d("KanjiViewModel", "Tessdata copied successfully to $trainedDataFile")
                } catch (e: IOException) {
                    Log.e("KanjiViewModel", "Failed to copy tessdata from assets", e)
                    tessBaseAPI = null
                    return
                }
            } else {
                Log.d("KanjiViewModel", "Tessdata already exists at $trainedDataFile")
            }

            // Инициализируем Tesseract с jpn
            val initialized = tessBaseAPI!!.init(dataPath, "jpn")
            if (!initialized) {
                Log.e("KanjiViewModel", "Could not initialize Tesseract")
                tessBaseAPI = null
            } else {
                Log.d("KanjiViewModel", "Tesseract initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("KanjiViewModel", "Error initializing Tesseract", e)
            tessBaseAPI = null
        }
    }

    private fun loadKanjiData(context: Context) {
        try {
            val json = context.assets.open("kanji_database.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Kanji>>() {}.type
            allKanji = Gson().fromJson(json, type)
            Log.d("KanjiViewModel", "Loaded ${allKanji.size} kanji from kanji_database.json")
        } catch (e: Exception) {
            Log.e("KanjiViewModel", "Error loading kanji data: ${e.message}", e)
            allKanji = emptyList()
        }
    }

    fun getKanjiBySymbol(symbol: String): Kanji? {
        return allKanji.find { it.kanji == symbol }
    }

    fun getAllKanji(): List<Kanji> {
        return allKanji
    }

    fun recognizeKanji(bitmap: Bitmap) {
        _isRecognizing.value = true // Устанавливаем состояние "загрузка"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("KanjiViewModel", "Starting recognizeKanji with bitmap: ${bitmap.width}x${bitmap.height}")
                if (tessBaseAPI == null) {
                    Log.e("KanjiViewModel", "Tesseract is not initialized")
                    _recognizedKanji.value = emptyList()
                    _isRecognizing.value = false
                    return@launch
                }

                // Устанавливаем изображение для распознавания
                Log.d("KanjiViewModel", "Setting image for Tesseract")
                tessBaseAPI!!.setImage(bitmap)

                // Добавляем тайм-аут для распознавания
                val recognizedText = withTimeoutOrNull(10000) { // 10 секунд тайм-аут
                    tessBaseAPI!!.utF8Text
                }

                if (recognizedText == null) {
                    Log.e("KanjiViewModel", "Tesseract recognition timed out after 10 seconds")
                    _recognizedKanji.value = emptyList()
                    _isRecognizing.value = false
                    return@launch
                }

                Log.d("KanjiViewModel", "Raw Tesseract output: '$recognizedText'")

                // Разделяем текст на отдельные иероглифы
                val cleanedText = recognizedText
                    .replace("\n", "")
                    .replace(" ", "")
                Log.d("KanjiViewModel", "Cleaned text after removing newlines and spaces: '$cleanedText'")

                val splitText = cleanedText.split("")
                Log.d("KanjiViewModel", "Split text into characters: $splitText")

                val kanjiList = splitText
                    .filter { char -> char.isNotEmpty() && char.matches("[\\p{InCJKUnifiedIdeographs}]".toRegex()) }
                Log.d("KanjiViewModel", "Filtered Kanji list: $kanjiList")

                _recognizedKanji.value = kanjiList
                _isRecognizing.value = false

                // Сохраняем распознанные иероглифы в коллекцию "Распознанные кандзи"
                if (kanjiList.isNotEmpty()) {
                    val kanjiString = kanjiList.joinToString(",")
                    val capturedCollection = collections.value.find { it.name == "Распознанные кандзи" }
                    if (capturedCollection != null) {
                        // Преобразуем список в MutableList один раз и используем его
                        val updatedKanjiList = capturedCollection.kanjiList.split(",").filter { it.isNotEmpty() }.toMutableList()
                        kanjiList.forEach { kanji ->
                            if (kanji !in updatedKanjiList) {
                                updatedKanjiList.add(kanji)
                            }
                        }
                        val updatedCollection = capturedCollection.copy(kanjiList = updatedKanjiList.joinToString(","))
                        repository.updateCollection(updatedCollection)
                    } else {
                        // Создаём новую коллекцию "Распознанные кандзи"
                        val newCollection = KanjiCollection(name = "Распознанные кандзи", kanjiList = kanjiString)
                        repository.insertCollection(newCollection)
                    }
                }
            } catch (e: Exception) {
                Log.e("KanjiViewModel", "Error recognizing Kanji: ${e.message}", e)
                _recognizedKanji.value = emptyList()
                _isRecognizing.value = false
            }
        }
    }

    private fun fetchCollections() {
        viewModelScope.launch {
            try {
                repository.getAllCollections().collect { collections: List<KanjiCollection> ->
                    _collections.value = collections
                    Log.d("KanjiViewModel", "Collections fetched: ${collections.size} items - $collections")
                }
            } catch (e: Exception) {
                Log.e("KanjiViewModel", "Error fetching collections", e)
            }
        }
    }

    fun searchCollections(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                fetchCollections()
                Log.d("KanjiViewModel", "Search query empty, fetching all collections")
            } else {
                repository.searchCollections(query).collect { collections: List<KanjiCollection> ->
                    _collections.value = collections
                    Log.d("KanjiViewModel", "Search query '$query', found ${collections.size} collections: $collections")
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
                Log.d("KanjiViewModel", "Added kanji '$kanji' to collection '${collection.name}', new kanjiList: ${updatedCollection.kanjiList}")
            } else {
                Log.d("KanjiViewModel", "Kanji '$kanji' already in collection '${collection.name}'")
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

    fun testInsertCollection() {
        viewModelScope.launch {
            val testCollection = KanjiCollection(name = "Test Collection", kanjiList = "日,月")
            repository.insertCollection(testCollection)
            Log.d("KanjiViewModel", "Test collection inserted")
        }
    }

    override fun onCleared() {
        tessBaseAPI?.end()
        super.onCleared()
    }
}