package com.codex.voicereminder.data.repository

import com.codex.voicereminder.data.dao.NoteDao
import com.codex.voicereminder.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()

    suspend fun insert(note: NoteEntity): Long = dao.insert(note)

    suspend fun update(note: NoteEntity) = dao.update(note)

    suspend fun delete(note: NoteEntity) = dao.delete(note)
}
