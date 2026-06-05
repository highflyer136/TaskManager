package com.taskmanager.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.taskmanager.data.repository.TaskRepository
import com.taskmanager.model.Priority
import com.taskmanager.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task

    private var taskId: String? = null

    fun loadTask(id: String) {
        taskId = id
        viewModelScope.launch {
            _task.value = repository.getTaskById(id)
        }
    }

    fun updateTask(
        title: String,
        description: String,
        priority: Priority,
        dueDate: Date?,
        isCompleted: Boolean
    ) {
        val current = _task.value ?: return
        viewModelScope.launch {
            val updated = repository.updateTask(
                current.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    isCompleted = isCompleted
                )
            )
            _task.value = updated
        }
    }

    fun toggleCompletion() {
        val id = taskId ?: return
        viewModelScope.launch {
            repository.toggleTaskCompletion(id)
            _task.value = repository.getTaskById(id)
        }
    }

    fun deleteTask() {
        val id = taskId ?: return
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.deleteTask(id, uid)
        }
    }
}
