package com.codex.voicereminder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.codex.voicereminder.R
import com.codex.voicereminder.data.model.Priority

object NotificationHelper {
    const val CHANNEL_HIGH = "reminders_high"
    const val CHANNEL_MEDIUM = "reminders_medium"
    const val CHANNEL_LOW = "reminders_low"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channels = listOf(
            NotificationChannel(
                CHANNEL_HIGH,
                context.getString(R.string.channel_high_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_high_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
            },
            NotificationChannel(
                CHANNEL_MEDIUM,
                context.getString(R.string.channel_medium_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_medium_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200)
                setSound(soundUri, audioAttributes)
            },
            NotificationChannel(
                CHANNEL_LOW,
                context.getString(R.string.channel_low_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_low_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 120)
                setSound(soundUri, audioAttributes)
            }
        )

        manager.createNotificationChannels(channels)
    }

    fun channelFor(priority: Priority): String = when (priority) {
        Priority.HIGH -> CHANNEL_HIGH
        Priority.MEDIUM -> CHANNEL_MEDIUM
        Priority.LOW -> CHANNEL_LOW
    }
}
