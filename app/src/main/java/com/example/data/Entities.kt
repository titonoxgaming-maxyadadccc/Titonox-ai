package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "General",
    val folder: String = "Notes",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val task: String,
    val isCompleted: Boolean = false,
    val priority: String = "Normal",
    val category: String = "General",
    val dueDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetTimeMillis: Long,
    val formattedTime: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateString: String, // e.g. "2026-09-23"
    val timeString: String = "10:00 AM",
    val startTimeMillis: Long = System.currentTimeMillis(),
    val reminderMinutesBefore: Int = 15,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaStoreId: Long = 0,
    val title: String,
    val artist: String = "TITONOX Studio",
    val album: String = "Cyber Resonance",
    val duration: Long = 180000L,
    val contentUri: String = "",
    val genre: String = "Electronic",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "UserPreference",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val state: String,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_models")
data class CustomModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerName: String,
    val modelName: String,
    val modelId: String,
    val apiBaseUrl: String,
    val apiKey: String,
    val requestFormat: String = "OpenAI",
    val responseFormat: String = "OpenAI",
    val authHeader: String = "Bearer",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val contextWindow: Int = 32768,
    val visionSupport: Boolean = false,
    val toolCalling: Boolean = true,
    val streaming: Boolean = true,
    val isSelected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

