package com.taskmanager.data.repository

import com.taskmanager.data.local.dao.TaskDao
import com.taskmanager.data.local.toDomain
import com.taskmanager.data.local.toEntity
import com.taskmanager.data.remote.FirestoreDataSource
import com.taskmanager.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val firestoreDataSource: FirestoreDataSource

) {

    fun getAllTasks(userId: String): Flow<List<Task>> =
        taskDao.getAllTasks(userId).map { list -> list.map { it.toDomain() } }

    fun getActiveTasks(userId: String): Flow<List<Task>> =
        taskDao.getActiveTasks(userId).map { list -> list.map { it.toDomain() } }

    fun getCompletedTasks(userId: String): Flow<List<Task>> =
        taskDao.getCompletedTasks(userId).map { list -> list.map { it.toDomain() } }

    fun getActiveTaskCount(userId: String): Flow<Int> =
        taskDao.getActiveTaskCount(userId)

    suspend fun getTaskById(taskId: String): Task? =
        taskDao.getTaskById(taskId)?.toDomain()

    suspend fun createTask(
        userId: String,
        title: String,
        description: String,
        priority: com.taskmanager.model.Priority,
        dueDate: Date?
    ): Task {
        val now = Date()
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            isCompleted = false,
            dueDate = dueDate,
            createdAt = now,
            updatedAt = now,
            userId = userId,
            isSynced = false
        )
        taskDao.insertTask(task.toEntity())
        syncTaskToFirestore(task)
        return task
    }

    suspend fun updateTask(task: Task): Task {
        val updated = task.copy(updatedAt = Date(), isSynced = false)
        taskDao.updateTask(updated.toEntity())
        syncTaskToFirestore(updated)
        return updated
    }

    suspend fun toggleTaskCompletion(taskId: String): Boolean {
        val task = taskDao.getTaskById(taskId) ?: return false
        val newState = !task.isCompleted
        val now = System.currentTimeMillis()
        taskDao.updateTaskCompletion(taskId, newState, now)
        val updatedTask = task.copy(isCompleted = newState, updatedAt = now, isSynced = false)
        syncTaskToFirestore(updatedTask.toDomain())
        return newState
    }

    suspend fun deleteTask(taskId: String, userId: String) {
        val now = System.currentTimeMillis()
        taskDao.softDeleteTask(taskId, now)
        try {
            firestoreDataSource.deleteTask(userId, taskId)
        } catch (e: Exception) {
            // Will retry on next sync
        }
    }

    suspend fun syncWithFirestore(userId: String) {
        try {
            // Push unsynced local changes
            val unsyncedTasks = taskDao.getUnsyncedTasks(userId)
            if (unsyncedTasks.isNotEmpty()) {
                firestoreDataSource.syncBatch(unsyncedTasks.map { it.toDomain() })
                unsyncedTasks.forEach { task ->
                    taskDao.updateTask(task.copy(isSynced = true))
                }
            }

            // Pull remote tasks
            val remoteTasks = firestoreDataSource.fetchAllTasks(userId)
            taskDao.insertTasks(remoteTasks.map { it.toEntity().copy(isSynced = true) })

            // Cleanup
            taskDao.clearSyncedDeletedTasks(userId)
        } catch (e: Exception) {
            // Sync failed - local data is still available
        }
    }

    private suspend fun syncTaskToFirestore(task: Task) {
        try {
            firestoreDataSource.syncTask(task)
            taskDao.updateTask(task.toEntity().copy(isSynced = true))
        } catch (e: Exception) {
            // Will sync later
        }
    }
}
