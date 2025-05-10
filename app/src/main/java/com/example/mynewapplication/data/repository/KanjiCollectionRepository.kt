package com.example.mynewapplication.data.repository

import com.example.mynewapplication.data.dao.KanjiCollectionDao
import com.example.mynewapplication.data.model.KanjiCollection
import kotlinx.coroutines.flow.Flow

class KanjiCollectionRepository(private val dao: KanjiCollectionDao) {
    fun getAllCollections(): Flow<List<KanjiCollection>> {
        return dao.getAllCollections()
    }

    fun searchCollections(query: String): Flow<List<KanjiCollection>> {
        return dao.searchCollections(query)
    }

    suspend fun insertCollection(collection: KanjiCollection) {
        dao.insertCollection(collection)
    }

    suspend fun updateCollection(collection: KanjiCollection) {
        dao.updateCollection(collection)
    }

    suspend fun deleteCollection(collection: KanjiCollection) {
        dao.deleteCollection(collection)
    }
}