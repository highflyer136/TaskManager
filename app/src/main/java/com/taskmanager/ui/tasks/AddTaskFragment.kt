package com.taskmanager.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.taskmanager.R
import com.taskmanager.databinding.FragmentAddTaskBinding
import com.taskmanager.model.Priority
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TasksViewModel by viewModels()
    private var selectedDueDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnPickDate.setOnClickListener { showDatePicker() }

        binding.btnClearDate.setOnClickListener {
            selectedDueDate = null
            binding.tvSelectedDate.text = getString(R.string.no_due_date)
            binding.btnClearDate.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            if (title.isEmpty()) {
                binding.etTitle.error = getString(R.string.error_title_required)
                return@setOnClickListener
            }
            val priority = when (binding.radioGroupPriority.checkedRadioButtonId) {
                R.id.radioPriorityHigh -> Priority.HIGH
                R.id.radioPriorityLow -> Priority.LOW
                else -> Priority.MEDIUM
            }
            viewModel.createTask(title, description, priority, selectedDueDate)
            findNavController().navigateUp()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 23, 59, 59)
                selectedDueDate = cal.time
                val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvSelectedDate.text = fmt.format(selectedDueDate!!)
                binding.btnClearDate.visibility = View.VISIBLE
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
