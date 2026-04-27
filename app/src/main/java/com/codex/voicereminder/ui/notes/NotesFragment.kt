package com.codex.voicereminder.ui.notes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codex.voicereminder.R
import com.codex.voicereminder.databinding.FragmentNotesBinding
import com.codex.voicereminder.ui.AppViewModelFactory
import kotlinx.coroutines.launch

class NotesFragment : Fragment(R.layout.fragment_notes) {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application)
    }

    private val adapter by lazy {
        NoteAdapter(
            onEdit = { note ->
                NoteEditorDialogFragment.newInstance(note).show(
                    parentFragmentManager,
                    NoteEditorDialogFragment.TAG
                )
            },
            onDelete = { note ->
                viewModel.deleteNote(note)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collect { notes ->
                    adapter.submitList(notes)
                    binding.emptyState.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
