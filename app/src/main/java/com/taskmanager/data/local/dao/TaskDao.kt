package com.taskmanager.data.local.dao

import androidx.room.*
import com.taskmanager.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 AND isDeleted = 0 ORDER BY dueDate ASC, createdAt DESC")
    fun getActiveTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getCompletedTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedTasks(userId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, updatedAt = :updatedAt, isSynced = 0 WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean, updatedAt: Long)

    @Query("UPDATE tasks SET isDeleted = 1, updatedAt = :updatedAt, isSynced = 0 WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: String, updatedAt: Long)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE userId = :userId AND isDeleted = 1 AND isSynced = 1")
    suspend fun clearSyncedDeletedTasks(userId: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND isCompleted = 0 AND isDeleted = 0")
    fun getActiveTaskCount(userId: String): Flow<Int>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isDeleted = 0 AND priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(userId: String, priority: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isDeleted = 0 AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getTasksInDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<TaskEntity>>
}
