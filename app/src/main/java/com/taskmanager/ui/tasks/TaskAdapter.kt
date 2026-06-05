package com.taskmanager.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmanager.R
import com.taskmanager.databinding.ItemTaskBinding
import com.taskmanager.model.Priority
import com.taskmanager.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvDescription.text = task.description
                checkboxTask.isChecked = task.isCompleted

                // Strikethrough for completed
                if (task.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTitle.alpha = 0.5f
                } else {
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTitle.alpha = 1.0f
                }

                // Due date
                task.dueDate?.let {
                    tvDueDate.text = dateFormatter.format(it)
                    tvDueDate.visibility = android.view.View.VISIBLE
                    if (it.before(Date()) && !task.isCompleted) {
                        tvDueDate.setTextColor(
                            ContextCompat.getColor(root.context, R.color.colorError)
                        )
                    } else {
                        tvDueDate.setTextColor(
                            ContextCompat.getColor(root.context, R.color.colorOnSurfaceVariant)
                        )
                    }
                } ?: run {
                    tvDueDate.visibility = android.view.View.GONE
                }

                // Priority chip
                val (priorityText, priorityColor) = when (task.priority) {
                    Priority.HIGH -> Pair(
                        root.context.getString(R.string.priority_high),
                        R.color.priorityHigh
                    )
                    Priority.MEDIUM -> Pair(
                        root.context.getString(R.string.priority_medium),
                        R.color.priorityMedium
                    )
                    Priority.LOW -> Pair(
                        root.context.getString(R.string.priority_low),
                        R.color.priorityLow
                    )
                }
                chipPriority.text = priorityText
                chipPriority.setChipBackgroundColorResource(priorityColor)

                // Sync indicator
                ivSyncStatus.setImageResource(
                    if (task.isSynced) R.drawable.ic_cloud_done else R.drawable.ic_cloud_off
                )

                // Clicks
                root.setOnClickListener { onTaskClick(task) }
                checkboxTask.setOnClickListener { onCheckboxClick(task) }
                ibDelete.setOnClickListener { onDeleteClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
