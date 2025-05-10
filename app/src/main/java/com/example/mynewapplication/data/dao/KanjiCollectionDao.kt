package com.example.mynewapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mynewapplication.data.model.KanjiCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface KanjiCollectionDao {
    @Query("SELECT * FROM kanji_collections")
    fun getAllCollections(): Flow<List<KanjiCollection>>

    @Query("SELECT * FROM kanji_collections WHERE name LIKE '%' || :query || '%'")
    fun searchCollections(query: String): Flow<List<KanjiCollection>>

    @Insert
    suspend fun insertCollection(collection: KanjiCollection)

    @Update
    suspend fun updateCollection(collection: KanjiCollection)

    @Delete
    suspend fun deleteCollection(collection: KanjiCollection)
}