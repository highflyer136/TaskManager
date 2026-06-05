package com.taskmanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val priority: Int, // 0=Low, 1=Medium, 2=High
    val isCompleted: Boolean,
    val dueDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val userId: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
