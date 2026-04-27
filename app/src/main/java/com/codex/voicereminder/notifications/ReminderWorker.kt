package com.codex.voicereminder.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codex.voicereminder.R
import com.codex.voicereminder.data.db.AppDatabase
import com.codex.voicereminder.data.model.ReminderStatus
import com.codex.voicereminder.ui.MainActivity

class ReminderWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        if (reminderId <= 0L) return Result.failure()

        val database = AppDatabase.getInstance(applicationContext)
        val reminder = database.reminderDao().getById(reminderId) ?: return Result.failure()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            reminder.id.toInt(),
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.channelFor(reminder.priority)
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(reminder.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.text))
            .setPriority(
                when (reminder.priority) {
                    com.codex.voicereminder.data.model.Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
                    com.codex.voicereminder.data.model.Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
                    com.codex.voicereminder.data.model.Priority.LOW -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU

        if (hasPermission) {
            NotificationManagerCompat.from(applicationContext).notify(reminder.id.toInt(), notification)
        }

        database.reminderDao().updateStatus(reminder.id, ReminderStatus.TRIGGERED)
        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
    }
}
