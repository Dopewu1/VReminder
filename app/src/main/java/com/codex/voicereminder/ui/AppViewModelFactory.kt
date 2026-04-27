package com.codex.voicereminder.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codex.voicereminder.data.db.AppDatabase
import com.codex.voicereminder.data.repository.NoteRepository
import com.codex.voicereminder.data.repository.ReminderRepository
import com.codex.voicereminder.notifications.ReminderScheduler
import com.codex.voicereminder.ui.notes.NotesViewModel
import com.codex.voicereminder.ui.reminders.RemindersViewModel

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val database by lazy { AppDatabase.getInstance(application) }
    private val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }
    private val noteRepository by lazy { NoteRepository(database.noteDao()) }
    private val scheduler by lazy { ReminderScheduler(application) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RemindersViewModel::class.java) -> {
                RemindersViewModel(reminderRepository, scheduler) as T
            }

            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(noteRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
