package com.codex.voicereminder.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codex.voicereminder.data.model.NoteEntity
import com.codex.voicereminder.databinding.ItemNoteBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class NoteAdapter(
    private val onEdit: (NoteEntity) -> Unit,
    private val onDelete: (NoteEntity) -> Unit
) : ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(
        private val binding: ItemNoteBinding,
        private val onEdit: (NoteEntity) -> Unit,
        private val onDelete: (NoteEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("ru"))

        fun bind(item: NoteEntity) {
            binding.noteText.text = item.text
            binding.createdAtText.text = Instant.ofEpochMilli(item.createdAtMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
            binding.editButton.setOnClickListener { onEdit(item) }
            binding.deleteButton.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private object DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
            override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
