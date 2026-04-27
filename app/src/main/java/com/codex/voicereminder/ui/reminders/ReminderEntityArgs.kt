package com.codex.voicereminder.ui.reminders

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.data.model.ReminderEntity
import com.codex.voicereminder.data.model.ReminderStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReminderEntityArgs(
    val id: Long,
    val text: String,
    val dateTimeMillis: Long,
    val priority: String,
    val status: String
) : Parcelable {
    fun toReminderEntity(): ReminderEntity = ReminderEntity(
        id = id,
        text = text,
        dateTimeMillis = dateTimeMillis,
        priority = Priority.valueOf(priority),
        status = ReminderStatus.valueOf(status)
    )

    companion object {
        fun from(reminder: ReminderEntity): ReminderEntityArgs = ReminderEntityArgs(
            id = reminder.id,
            text = reminder.text,
            dateTimeMillis = reminder.dateTimeMillis,
            priority = reminder.priority.name,
            status = reminder.status.name
        )
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return BundleCompat.getParcelable(this, key, T::class.java)
}
