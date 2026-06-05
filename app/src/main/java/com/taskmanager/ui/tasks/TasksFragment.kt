package com.taskmanager.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.taskmanager.R
import com.taskmanager.databinding.FragmentTasksBinding
import com.taskmanager.ui.auth.AuthActivity
import com.taskmanager.utils.isTablet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TasksViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupRecyclerView()
        setupTabs()
        setupFab()
        observeViewModel()
        updateGreeting()
    }

    private fun setupMenu() {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_tasks, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_sync -> {
                        viewModel.syncWithCloud()
                        true
                    }
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_tasksFragment_to_settingsFragment)
                        true
                    }
                    R.id.action_sign_out -> {
                        viewModel.signOut {
                            startActivity(
                                Intent(requireContext(), AuthActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                            requireActivity().finish()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = Bundle().apply { putString("taskId", task.id) }
                findNavController().navigate(R.id.action_tasksFragment_to_taskDetailFragment, bundle)
            },
            onCheckboxClick = { task -> viewModel.toggleTaskCompletion(task.id) },
            onDeleteClick = { task -> viewModel.deleteTask(task.id) }
        )
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setTab(TaskTab.ALL)
                    1 -> viewModel.setTab(TaskTab.ACTIVE)
                    2 -> viewModel.setTab(TaskTab.COMPLETED)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment_to_addTaskFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is TasksUiState.Loading
            when (state) {
                is TasksUiState.Success -> {
                    taskAdapter.submitList(state.tasks)
                    binding.tvEmpty.isVisible = state.tasks.isEmpty()
                    binding.recyclerViewTasks.isVisible = true
                }
                is TasksUiState.Error -> {
                    binding.recyclerViewTasks.isVisible = false
                    binding.tvEmpty.isVisible = true
                    binding.tvEmpty.text = state.message
                }
                else -> {}
            }
        }

        viewModel.activeTaskCount.observe(viewLifecycleOwner) { count ->
            binding.tvTaskCount.text = resources.getQuantityString(
                R.plurals.active_tasks_count, count, count
            )
        }
    }

    private fun updateGreeting() {
        binding.tvGreeting.text = getString(
            R.string.greeting,
            viewModel.userName ?: getString(R.string.default_user_name)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewTasks.adapter = null  // prevent leaking the adapter
        _binding = null
    }
}
