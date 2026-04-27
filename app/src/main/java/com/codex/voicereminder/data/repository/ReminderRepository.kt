package com.codex.voicereminder.data.repository

import com.codex.voicereminder.data.dao.ReminderDao
import com.codex.voicereminder.data.model.ReminderEntity
import com.codex.voicereminder.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun getById(id: Long): ReminderEntity? = dao.getById(id)

    suspend fun insert(reminder: ReminderEntity): Long = dao.insert(reminder)

    suspend fun update(reminder: ReminderEntity) = dao.update(reminder)

    suspend fun delete(reminder: ReminderEntity) = dao.delete(reminder)

    suspend fun updateStatus(id: Long, status: ReminderStatus) = dao.updateStatus(id, status)
}
