package com.taskmanager.ui.detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.taskmanager.R
import com.taskmanager.databinding.FragmentTaskDetailBinding
import com.taskmanager.model.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskDetailViewModel by viewModels()
    private var selectedDueDate: Date? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val taskId = arguments?.getString("taskId") ?: return
        viewModel.loadTask(taskId)
        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.task.observe(viewLifecycleOwner) { task ->
            task ?: return@observe
            selectedDueDate = task.dueDate
            val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            binding.apply {
                etTitle.setText(task.title)
                etDescription.setText(task.description)
                checkboxCompleted.isChecked = task.isCompleted
                tvCreatedAt.text = getString(R.string.created_at, fmt.format(task.createdAt))
                tvUpdatedAt.text = getString(R.string.updated_at, fmt.format(task.updatedAt))
                tvSyncStatus.text = if (task.isSynced)
                    getString(R.string.synced)
                else
                    getString(R.string.not_synced)

                task.dueDate?.let {
                    tvSelectedDate.text = fmt.format(it)
                } ?: run {
                    tvSelectedDate.text = getString(R.string.no_due_date)
                }

                when (task.priority) {
                    Priority.HIGH -> radioGroupPriority.check(R.id.radioPriorityHigh)
                    Priority.MEDIUM -> radioGroupPriority.check(R.id.radioPriorityMedium)
                    Priority.LOW -> radioGroupPriority.check(R.id.radioPriorityLow)
                }

                setEditMode(false)
            }
        }
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            isEditMode = true
            setEditMode(true)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty()) {
                binding.etTitle.error = getString(R.string.error_title_required)
                return@setOnClickListener
            }
            val priority = when (binding.radioGroupPriority.checkedRadioButtonId) {
                R.id.radioPriorityHigh -> Priority.HIGH
                R.id.radioPriorityLow -> Priority.LOW
                else -> Priority.MEDIUM
            }
            viewModel.updateTask(
                title = title,
                description = binding.etDescription.text.toString().trim(),
                priority = priority,
                dueDate = selectedDueDate,
                isCompleted = binding.checkboxCompleted.isChecked
            )
            setEditMode(false)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteTask()
            findNavController().navigateUp()
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }

        binding.checkboxCompleted.setOnClickListener {
            if (!isEditMode) {
                viewModel.toggleCompletion()
            }
        }
    }

    private fun setEditMode(editing: Boolean) {
        isEditMode = editing
        binding.apply {
            etTitle.isEnabled = editing
            etDescription.isEnabled = editing
            radioGroupPriority.isEnabled = editing
            radioPriorityHigh.isEnabled = editing
            radioPriorityMedium.isEnabled = editing
            radioPriorityLow.isEnabled = editing
            btnPickDate.isVisible = editing
            btnEdit.isVisible = !editing
            btnSave.isVisible = editing
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDueDate?.let { cal.time = it }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 23, 59, 59)
                selectedDueDate = cal.time
                val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvSelectedDate.text = fmt.format(selectedDueDate!!)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
