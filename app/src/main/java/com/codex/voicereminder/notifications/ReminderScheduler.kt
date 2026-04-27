package com.codex.voicereminder.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codex.voicereminder.data.model.ReminderEntity
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ReminderScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun schedule(reminder: ReminderEntity) {
        val delayMillis = max(1000L, reminder.dateTimeMillis - System.currentTimeMillis())
        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(workName(reminder.id))
            .build()

        workManager.enqueueUniqueWork(
            workName(reminder.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(reminderId: Long) {
        workManager.cancelUniqueWork(workName(reminderId))
    }

    private fun workName(reminderId: Long): String = "reminder_$reminderId"
}
