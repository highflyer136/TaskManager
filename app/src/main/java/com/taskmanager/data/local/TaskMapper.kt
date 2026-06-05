package com.taskmanager.data.local

import com.taskmanager.data.local.entities.TaskEntity
import com.taskmanager.model.Priority
import com.taskmanager.model.Task
import java.util.Date

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    priority = Priority.fromInt(priority),
    isCompleted = isCompleted,
    dueDate = dueDate?.let { Date(it) },
    createdAt = Date(createdAt),
    updatedAt = Date(updatedAt),
    userId = userId,
    isSynced = isSynced
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority.value,
    isCompleted = isCompleted,
    dueDate = dueDate?.time,
    createdAt = createdAt.time,
    updatedAt = updatedAt.time,
    userId = userId,
    isSynced = isSynced
)
