package com.codex.voicereminder.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codex.voicereminder.R
import com.codex.voicereminder.data.model.Priority
import com.codex.voicereminder.data.model.ReminderEntity
import com.codex.voicereminder.data.model.ReminderStatus
import com.codex.voicereminder.databinding.ItemReminderBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReminderAdapter(
    private val onEdit: (ReminderEntity) -> Unit,
    private val onDelete: (ReminderEntity) -> Unit
) : ListAdapter<ReminderEntity, ReminderAdapter.ReminderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReminderViewHolder(
        private val binding: ItemReminderBinding,
        private val onEdit: (ReminderEntity) -> Unit,
        private val onDelete: (ReminderEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("ru"))

        fun bind(item: ReminderEntity) {
            binding.titleText.text = item.text
            binding.dateTimeText.text = Instant.ofEpochMilli(item.dateTimeMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
            binding.priorityChip.text = when (item.priority) {
                Priority.HIGH -> binding.root.context.getString(R.string.priority_high)
                Priority.MEDIUM -> binding.root.context.getString(R.string.priority_medium)
                Priority.LOW -> binding.root.context.getString(R.string.priority_low)
            }
            binding.priorityChip.setChipBackgroundColorResource(
                when (item.priority) {
                    Priority.HIGH -> R.color.priority_high_container
                    Priority.MEDIUM -> R.color.priority_medium_container
                    Priority.LOW -> R.color.priority_low_container
                }
            )
            binding.priorityChip.setTextColor(
                binding.root.context.getColor(
                    when (item.priority) {
                        Priority.HIGH -> R.color.priority_high
                        Priority.MEDIUM -> R.color.priority_medium
                        Priority.LOW -> R.color.priority_low
                    }
                )
            )
            binding.statusText.text = when (item.status) {
                ReminderStatus.ACTIVE -> binding.root.context.getString(R.string.status_active)
                ReminderStatus.TRIGGERED -> binding.root.context.getString(R.string.status_triggered)
            }
            binding.editButton.setOnClickListener { onEdit(item) }
            binding.deleteButton.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private object DiffCallback : DiffUtil.ItemCallback<ReminderEntity>() {
            override fun areItemsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
