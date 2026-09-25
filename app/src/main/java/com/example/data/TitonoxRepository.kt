package com.example.data

import kotlinx.coroutines.flow.Flow

data class UniversalSearchResult(
    val category: String, // "Notes", "Todos", "Calendar", "Music"
    val title: String,
    val subtitle: String,
    val targetType: String,
    val id: Long
) {
    val type: String get() = targetType
    val snippet: String get() = subtitle
}

class TitonoxRepository(private val db: AppDatabase) {
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    val allTodos: Flow<List<TodoEntity>> = db.todoDao().getAllTodos()
    val allReminders: Flow<List<ReminderEntity>> = db.reminderDao().getAllReminders()
    val allEvents: Flow<List<CalendarEventEntity>> = db.calendarEventDao().getAllEvents()
    val allSongs: Flow<List<SongEntity>> = db.songDao().getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = db.songDao().getFavoriteSongs()
    val allMemories: Flow<List<MemoryEntity>> = db.memoryDao().getAllMemories()
    val recentTasks: Flow<List<TaskHistoryEntity>> = db.taskHistoryDao().getRecentTasks()

    fun getEventsForDate(date: String): Flow<List<CalendarEventEntity>> {
        return db.calendarEventDao().getEventsForDate(date)
    }

    suspend fun saveNote(
        title: String,
        content: String,
        tag: String = "General",
        folder: String = "Notes",
        isPinned: Boolean = false,
        isFavorite: Boolean = false
    ): Long {
        return db.noteDao().insertNote(
            NoteEntity(
                title = title,
                content = content,
                tag = tag,
                folder = folder,
                isPinned = isPinned,
                isFavorite = isFavorite
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        db.noteDao().updateNote(note)
    }

    suspend fun deleteNote(id: Long) {
        db.noteDao().deleteNoteById(id)
    }

    suspend fun addTodo(
        task: String,
        priority: String = "Normal",
        category: String = "General",
        dueDate: String = ""
    ): Long {
        return db.todoDao().insertTodo(
            TodoEntity(
                task = task,
                priority = priority,
                category = category,
                dueDate = dueDate
            )
        )
    }

    suspend fun toggleTodo(todo: TodoEntity) {
        db.todoDao().updateTodo(todo.copy(isCompleted = !todo.isCompleted))
    }

    suspend fun deleteTodo(id: Long) {
        db.todoDao().deleteTodoById(id)
    }

    suspend fun addCalendarEvent(
        title: String,
        description: String,
        dateString: String,
        timeString: String,
        reminderMinutes: Int = 15
    ): Long {
        return db.calendarEventDao().insertEvent(
            CalendarEventEntity(
                title = title,
                description = description,
                dateString = dateString,
                timeString = timeString,
                reminderMinutesBefore = reminderMinutes
            )
        )
    }

    suspend fun deleteCalendarEvent(id: Long) {
        db.calendarEventDao().deleteEventById(id)
    }

    suspend fun saveSongs(songs: List<SongEntity>) {
        db.songDao().insertSongs(songs)
    }

    suspend fun updateSong(song: SongEntity) {
        db.songDao().updateSong(song)
    }

    suspend fun addReminder(title: String, targetTimeMillis: Long, formattedTime: String): Long {
        return db.reminderDao().insertReminder(
            ReminderEntity(
                title = title,
                targetTimeMillis = targetTimeMillis,
                formattedTime = formattedTime
            )
        )
    }

    suspend fun completeReminder(id: Long) {
        db.reminderDao().markCompleted(id)
    }

    suspend fun deleteReminder(id: Long) {
        db.reminderDao().deleteReminderById(id)
    }

    suspend fun saveMemory(key: String, value: String, category: String = "UserPreference"): Long {
        return db.memoryDao().insertOrUpdateMemory(
            MemoryEntity(key = key, value = value, category = category)
        )
    }

    suspend fun getMemory(key: String): MemoryEntity? {
        return db.memoryDao().getMemoryByKey(key)
    }

    suspend fun deleteMemoryByKey(key: String) {
        db.memoryDao().deleteMemoryByKey(key)
    }

    suspend fun deleteMemoryById(id: Long) {
        db.memoryDao().deleteMemoryById(id)
    }

    suspend fun clearMemories() {
        db.memoryDao().clearAllMemories()
    }

    suspend fun recordTask(command: String, state: String, summary: String): Long {
        return db.taskHistoryDao().insertTask(
            TaskHistoryEntity(command = command, state = state, summary = summary)
        )
    }

    suspend fun searchUniversal(query: String): List<UniversalSearchResult> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val results = mutableListOf<UniversalSearchResult>()

        val notes = db.noteDao().searchNotes(q)
        notes.forEach { n ->
            results.add(
                UniversalSearchResult(
                    category = "Notes",
                    title = n.title,
                    subtitle = n.content.take(60),
                    targetType = "NOTE",
                    id = n.id
                )
            )
        }

        val todos = db.todoDao().searchTodos(q)
        todos.forEach { t ->
            results.add(
                UniversalSearchResult(
                    category = "Tasks",
                    title = t.task,
                    subtitle = "Priority: ${t.priority} • Status: ${if (t.isCompleted) "Completed" else "Pending"}",
                    targetType = "TODO",
                    id = t.id
                )
            )
        }

        val events = db.calendarEventDao().searchEvents(q)
        events.forEach { e ->
            results.add(
                UniversalSearchResult(
                    category = "Calendar",
                    title = e.title,
                    subtitle = "${e.dateString} at ${e.timeString}",
                    targetType = "EVENT",
                    id = e.id
                )
            )
        }

        val songs = db.songDao().searchSongs(q)
        songs.forEach { s ->
            results.add(
                UniversalSearchResult(
                    category = "Music",
                    title = s.title,
                    subtitle = "${s.artist} • ${s.album}",
                    targetType = "SONG",
                    id = s.id
                )
            )
        }

        return results
    }

    suspend fun deleteTask(id: Long) {
        db.taskHistoryDao().deleteTaskById(id)
    }

    suspend fun clearTaskHistory() {
        db.taskHistoryDao().clearTaskHistory()
    }

    val allCustomModels: Flow<List<CustomModelEntity>> = db.customModelDao().getAllCustomModels()

    suspend fun saveCustomModel(model: CustomModelEntity): Long {
        return db.customModelDao().insertModel(model)
    }

    suspend fun updateCustomModel(model: CustomModelEntity) {
        db.customModelDao().updateModel(model)
    }

    suspend fun deleteCustomModel(id: Long) {
        db.customModelDao().deleteModelById(id)
    }

    suspend fun selectCustomModel(id: Long) {
        db.customModelDao().setSelectedModel(id)
    }

    suspend fun getSelectedCustomModel(): CustomModelEntity? {
        return db.customModelDao().getSelectedCustomModel()
    }
}

