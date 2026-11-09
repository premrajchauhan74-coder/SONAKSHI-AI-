package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessageEntity::class, AutomationMacroEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SonakshiDatabase : RoomDatabase() {
    abstract fun sonakshiDao(): SonakshiDao

    companion object {
        @Volatile
        private var INSTANCE: SonakshiDatabase? = null

        fun getInstance(context: Context): SonakshiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonakshiDatabase::class.java,
                    "sonakshi_ai_assistant.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
