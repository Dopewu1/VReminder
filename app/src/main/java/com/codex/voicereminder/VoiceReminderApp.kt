package com.codex.voicereminder

import android.app.Application
import com.codex.voicereminder.notifications.NotificationHelper

class VoiceReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
