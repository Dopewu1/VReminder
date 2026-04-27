package com.codex.voicereminder.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codex.voicereminder.data.model.NoteEntity
import com.codex.voicereminder.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    val notes: StateFlow<List<NoteEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createNote(text: String) {
        viewModelScope.launch {
            repository.insert(NoteEntity(text = text, createdAtMillis = System.currentTimeMillis()))
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { repository.update(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.delete(note) }
    }
}
