package com.taskmanager.ui.tasks

import android.os.Bundle
import androidx.lifecycle.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.taskmanager.data.repository.TaskRepository
import com.taskmanager.model.Priority
import com.taskmanager.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed class TasksUiState {
    object Loading : TasksUiState()
    data class Success(val tasks: List<Task>) : TasksUiState()
    data class Error(val message: String) : TasksUiState()
}

enum class TaskTab { ALL, ACTIVE, COMPLETED }

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics
) : ViewModel() {

    private val _currentTab = MutableStateFlow(TaskTab.ALL)
    val currentTab: StateFlow<TaskTab> = _currentTab

    private val _uiState = MutableLiveData<TasksUiState>(TasksUiState.Loading)
    val uiState: LiveData<TasksUiState> = _uiState

    private val _activeTaskCount = MutableLiveData(0)
    val activeTaskCount: LiveData<Int> = _activeTaskCount

    val userId: String? get() = auth.currentUser?.uid
    val userName: String? get() = auth.currentUser?.displayName ?: auth.currentUser?.email

    init {
        loadTasks()
        observeActiveCount()
        syncWithCloud()
    }

    private fun loadTasks() {
        val uid = userId ?: return
        viewModelScope.launch {
            _currentTab.flatMapLatest { tab ->
                when (tab) {
                    TaskTab.ALL -> repository.getAllTasks(uid)
                    TaskTab.ACTIVE -> repository.getActiveTasks(uid)
                    TaskTab.COMPLETED -> repository.getCompletedTasks(uid)
                }
            }.collect { tasks ->
                _uiState.value = TasksUiState.Success(tasks)
            }
        }
    }

    private fun observeActiveCount() {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.getActiveTaskCount(uid).collect {
                _activeTaskCount.value = it
            }
        }
    }

    fun setTab(tab: TaskTab) {
        _currentTab.value = tab
    }

    fun createTask(
        title: String,
        description: String,
        priority: Priority,
        dueDate: Date?
    ) {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.createTask(uid, title, description, priority, dueDate)
            analytics.logEvent("task_created", Bundle().apply {
                putString("priority", priority.name)
                putBoolean("has_due_date", dueDate != null)
            })
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val completed = repository.toggleTaskCompletion(taskId)
            if (completed) {
                analytics.logEvent("task_completed", null)
            }
        }
    }

    fun deleteTask(taskId: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.deleteTask(taskId, uid)
            analytics.logEvent("task_deleted", null)
        }
    }

    fun syncWithCloud() {
        val uid = userId ?: return
        viewModelScope.launch {
            repository.syncWithFirestore(uid)
        }
    }

    fun signOut(onDone: () -> Unit) {
        auth.signOut()
        onDone()
    }
}
