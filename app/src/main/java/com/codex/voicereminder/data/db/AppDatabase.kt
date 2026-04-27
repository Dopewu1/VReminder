package com.codex.voicereminder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codex.voicereminder.data.dao.NoteDao
import com.codex.voicereminder.data.dao.ReminderDao
import com.codex.voicereminder.data.model.NoteEntity
import com.codex.voicereminder.data.model.ReminderEntity

@Database(
    entities = [ReminderEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_reminder.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
