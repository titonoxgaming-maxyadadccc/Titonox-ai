package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoteEntity::class,
        TodoEntity::class,
        ReminderEntity::class,
        CalendarEventEntity::class,
        SongEntity::class,
        MemoryEntity::class,
        TaskHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun todoDao(): TodoDao
    abstract fun reminderDao(): ReminderDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun songDao(): SongDao
    abstract fun memoryDao(): MemoryDao
    abstract fun taskHistoryDao(): TaskHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "titonox_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

