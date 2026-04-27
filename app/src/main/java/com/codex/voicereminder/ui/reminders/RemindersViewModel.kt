package com.codex.voicereminder.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.data.model.ReminderEntity
import com.codex.voicereminder.data.model.ReminderStatus
import com.codex.voicereminder.data.repository.ReminderRepository
import com.codex.voicereminder.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createReminder(text: String, dateTime: Long, priority: Priority) {
        viewModelScope.launch {
            val id = repository.insert(
                ReminderEntity(
                    text = text,
                    dateTimeMillis = dateTime,
                    priority = priority,
                    status = ReminderStatus.ACTIVE
                )
            )
            scheduler.schedule(
                ReminderEntity(
                    id = id,
                    text = text,
                    dateTimeMillis = dateTime,
                    priority = priority,
                    status = ReminderStatus.ACTIVE
                )
            )
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.update(reminder.copy(status = ReminderStatus.ACTIVE))
            scheduler.schedule(reminder.copy(status = ReminderStatus.ACTIVE))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            scheduler.cancel(reminder.id)
            repository.delete(reminder)
        }
    }
}
