package com.codex.voicereminder.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "datetime") val dateTimeMillis: Long,
    @ColumnInfo(name = "priority") val priority: Priority,
    @ColumnInfo(name = "status") val status: ReminderStatus = ReminderStatus.ACTIVE
)
