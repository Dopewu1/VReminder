package com.codex.voicereminder.ui.notes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.codex.voicereminder.R
import com.codex.voicereminder.data.model.NoteEntity
import com.codex.voicereminder.databinding.DialogNoteEditorBinding
import com.codex.voicereminder.ui.AppViewModelFactory
import com.codex.voicereminder.ui.reminders.getParcelableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize

class NoteEditorDialogFragment : DialogFragment() {
    private lateinit var binding: DialogNoteEditorBinding

    private val viewModel: NotesViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNoteEditorBinding.inflate(LayoutInflater.from(requireContext()))
        val note = arguments?.getParcelableCompat<NoteEntityArgs>(ARG_NOTE)?.toNoteEntity()
        binding.noteInput.setText(note?.text.orEmpty())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (note == null) R.string.add_note else R.string.edit_note)
            .setView(binding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (saveNote(note)) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun saveNote(existing: NoteEntity?): Boolean {
        val text = binding.noteInput.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.validation_empty_text, Toast.LENGTH_SHORT).show()
            return false
        }

        if (existing == null) {
            viewModel.createNote(text)
        } else {
            viewModel.updateNote(existing.copy(text = text))
        }
        return true
    }

    companion object {
        const val TAG = "NoteEditorDialog"
        private const val ARG_NOTE = "arg_note"

        fun newInstance(): NoteEditorDialogFragment = NoteEditorDialogFragment()

        fun newInstance(note: NoteEntity): NoteEditorDialogFragment = NoteEditorDialogFragment().apply {
            arguments = bundleOf(ARG_NOTE to NoteEntityArgs.from(note))
        }
    }
}

@Parcelize
data class NoteEntityArgs(
    val id: Long,
    val text: String,
    val createdAtMillis: Long
) : android.os.Parcelable {
    fun toNoteEntity(): NoteEntity = NoteEntity(
        id = id,
        text = text,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun from(note: NoteEntity): NoteEntityArgs = NoteEntityArgs(
            id = note.id,
            text = note.text,
            createdAtMillis = note.createdAtMillis
        )
    }
}
