package com.example.tencentWork.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Emoji::class], version = 1, exportSchema = false)
abstract class EmojiDatabase : RoomDatabase() {
    abstract fun emojiDao(): EmojiDao

    companion object {
        @Volatile
        private var INSTANCE: EmojiDatabase? = null

        fun getInstance(context: Context): EmojiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmojiDatabase::class.java,
                    "emoji_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}



