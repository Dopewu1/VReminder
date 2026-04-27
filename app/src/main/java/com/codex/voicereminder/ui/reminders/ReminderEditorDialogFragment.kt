package com.codex.voicereminder.ui.reminders

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.codex.voicereminder.R
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.data.model.ReminderEntity
import com.codex.voicereminder.databinding.DialogReminderEditorBinding
import com.codex.voicereminder.ui.AppViewModelFactory
import com.codex.voicereminder.util.RussianDateTimeParser
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReminderEditorDialogFragment : DialogFragment() {
    private val viewModel: RemindersViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application)
    }

    private val parser = RussianDateTimeParser()
    private lateinit var binding: DialogReminderEditorBinding
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now().plusHours(1).withMinute(0)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogReminderEditorBinding.inflate(LayoutInflater.from(requireContext()))

        val reminder = arguments?.getParcelableCompat<ReminderEntityArgs>(ARG_REMINDER)?.toReminderEntity()
        val initialText = arguments?.getString(ARG_TEXT).orEmpty().ifBlank { reminder?.text.orEmpty() }
        val initialDateTime = reminder?.dateTimeMillis?.let { parser.fromEpochMillis(it) }

        if (initialDateTime != null) {
            selectedDate = initialDateTime.toLocalDate()
            selectedTime = initialDateTime.toLocalTime()
        }

        binding.taskInput.setText(initialText)
        setupPriorityDropdown(reminder?.priority ?: Priority.MEDIUM)
        updateDateButton()
        updateTimeButton()

        binding.dateButton.setOnClickListener { showDatePicker() }
        binding.timeButton.setOnClickListener { showTimePicker() }

        val dialogTitle = if (reminder == null) R.string.add_reminder else R.string.edit_reminder

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(dialogTitle)
            .setView(binding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (saveReminder(reminder)) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun setupPriorityDropdown(priority: Priority) {
        val priorities = listOf(
            getString(R.string.priority_high),
            getString(R.string.priority_medium),
            getString(R.string.priority_low)
        )
        binding.priorityDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, priorities)
        )
        binding.priorityDropdown.setText(
            when (priority) {
                Priority.HIGH -> priorities[0]
                Priority.MEDIUM -> priorities[1]
                Priority.LOW -> priorities[2]
            },
            false
        )
    }

    private fun saveReminder(existing: ReminderEntity?): Boolean {
        val text = binding.taskInput.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.validation_empty_text, Toast.LENGTH_SHORT).show()
            return false
        }

        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
        val epochMillis = parser.toEpochMillis(dateTime)
        val priority = when (binding.priorityDropdown.text?.toString()) {
            getString(R.string.priority_high) -> Priority.HIGH
            getString(R.string.priority_low) -> Priority.LOW
            else -> Priority.MEDIUM
        }

        if (existing == null) {
            viewModel.createReminder(text, epochMillis, priority)
        } else {
            viewModel.updateReminder(
                existing.copy(
                    text = text,
                    dateTimeMillis = epochMillis,
                    priority = priority
                )
            )
        }
        return true
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            updateDateButton()
        }
        picker.show(parentFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(selectedTime.hour)
            .setMinute(selectedTime.minute)
            .build()
        picker.addOnPositiveButtonClickListener {
            selectedTime = LocalTime.of(picker.hour, picker.minute)
            updateTimeButton()
        }
        picker.show(parentFragmentManager, "time_picker")
    }

    private fun updateDateButton() {
        binding.dateButton.text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru")))
    }

    private fun updateTimeButton() {
        binding.timeButton.text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale("ru")))
    }

    companion object {
        const val TAG = "ReminderEditorDialog"
        private const val ARG_REMINDER = "arg_reminder"
        private const val ARG_TEXT = "arg_text"

        fun newInstance(): ReminderEditorDialogFragment = ReminderEditorDialogFragment()

        fun newInstance(text: String): ReminderEditorDialogFragment = ReminderEditorDialogFragment().apply {
            arguments = bundleOf(ARG_TEXT to text)
        }

        fun newInstance(reminder: ReminderEntity): ReminderEditorDialogFragment = ReminderEditorDialogFragment().apply {
            arguments = bundleOf(ARG_REMINDER to ReminderEntityArgs.from(reminder))
        }
    }
}
