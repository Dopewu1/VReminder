package com.codex.voicereminder.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codex.voicereminder.R
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.databinding.ActivityMainBinding
import com.codex.voicereminder.ui.notes.NoteEditorDialogFragment
import com.codex.voicereminder.ui.notes.NotesFragment
import com.codex.voicereminder.ui.reminders.ReminderEditorDialogFragment
import com.codex.voicereminder.ui.reminders.RemindersFragment
import com.codex.voicereminder.ui.reminders.RemindersViewModel
import com.codex.voicereminder.util.RussianDateTimeParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val parser = RussianDateTimeParser()
    private val viewModel: RemindersViewModel by viewModels {
        AppViewModelFactory(application)
    }

    private var currentTab = Tab.REMINDERS

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val voiceText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (voiceText.isNullOrBlank()) {
            showMessage(getString(R.string.voice_empty_error))
        } else {
            handleVoiceInput(voiceText)
        }
    }

    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchVoiceRecognition()
        } else {
            showMessage(getString(R.string.microphone_permission_denied))
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ensureNotificationPermission()
        setupBottomNavigation(savedInstanceState == null)
        setupFab()
    }

    private fun setupBottomNavigation(initialLoad: Boolean) {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_reminders -> switchTab(Tab.REMINDERS)
                R.id.menu_notes -> switchTab(Tab.NOTES)
            }
            true
        }

        if (initialLoad) {
            binding.bottomNavigation.selectedItemId = R.id.menu_reminders
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            when (currentTab) {
                Tab.REMINDERS -> showReminderCreationOptions()
                Tab.NOTES -> NoteEditorDialogFragment.newInstance().show(
                    supportFragmentManager,
                    NoteEditorDialogFragment.TAG
                )
            }
        }
    }

    private fun showReminderCreationOptions() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_reminder)
            .setItems(arrayOf(getString(R.string.action_voice), getString(R.string.action_manual))) { _, which ->
                when (which) {
                    0 -> requestVoiceRecognition()
                    1 -> ReminderEditorDialogFragment.newInstance().show(
                        supportFragmentManager,
                        ReminderEditorDialogFragment.TAG
                    )
                }
            }
            .show()
    }

    private fun requestVoiceRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showMessage(getString(R.string.voice_not_available))
            return
        }
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            launchVoiceRecognition()
        } else {
            microphonePermissionLauncher.launch(permission)
        }
    }

    private fun launchVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt))
        }

        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            showMessage(getString(R.string.voice_not_available))
        }
    }

    private fun handleVoiceInput(text: String) {
        val parsed = parser.parse(text)
        if (parsed.taskText.isBlank()) {
            showMessage(getString(R.string.voice_parse_task_error))
            return
        }

        if (parsed.dateTime == null) {
            ReminderEditorDialogFragment.newInstance(
                text = parsed.taskText
            ).show(supportFragmentManager, ReminderEditorDialogFragment.TAG)
            showMessage(getString(R.string.voice_parse_datetime_error))
            return
        }

        viewModel.createReminder(
            text = parsed.taskText,
            dateTime = parser.toEpochMillis(parsed.dateTime),
            priority = Priority.MEDIUM
        )
        Snackbar.make(binding.root, getString(R.string.reminder_created_voice), Snackbar.LENGTH_LONG).show()
    }

    private fun switchTab(tab: Tab) {
        currentTab = tab
        binding.toolbarTitle.text = when (tab) {
            Tab.REMINDERS -> getString(R.string.menu_reminders)
            Tab.NOTES -> getString(R.string.menu_notes)
        }

        val fragment = when (tab) {
            Tab.REMINDERS -> RemindersFragment()
            Tab.NOTES -> NotesFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    enum class Tab {
        REMINDERS,
        NOTES
    }
}
