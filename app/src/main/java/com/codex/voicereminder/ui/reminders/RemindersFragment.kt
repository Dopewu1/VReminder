package com.codex.voicereminder.ui.reminders

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codex.voicereminder.R
import com.codex.voicereminder.databinding.FragmentRemindersBinding
import com.codex.voicereminder.ui.AppViewModelFactory
import kotlinx.coroutines.launch

class RemindersFragment : Fragment(R.layout.fragment_reminders) {
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RemindersViewModel by activityViewModels {
        AppViewModelFactory(requireActivity().application)
    }

    private val adapter by lazy {
        ReminderAdapter(
            onEdit = { reminder ->
                ReminderEditorDialogFragment.newInstance(reminder).show(
                    parentFragmentManager,
                    ReminderEditorDialogFragment.TAG
                )
            },
            onDelete = { reminder ->
                viewModel.deleteReminder(reminder)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRemindersBinding.bind(view)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminders.collect { reminders ->
                    adapter.submitList(reminders)
                    binding.emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
