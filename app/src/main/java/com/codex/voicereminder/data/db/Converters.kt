package com.codex.voicereminder.data.db

import androidx.room.TypeConverter
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.data.model.ReminderStatus

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)
}
