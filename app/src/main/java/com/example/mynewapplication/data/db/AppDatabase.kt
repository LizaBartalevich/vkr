package com.example.mynewapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mynewapplication.data.dao.KanjiCollectionDao
import com.example.mynewapplication.data.model.KanjiCollection

@Database(entities = [KanjiCollection::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kanjiCollectionDao(): KanjiCollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kanji_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}