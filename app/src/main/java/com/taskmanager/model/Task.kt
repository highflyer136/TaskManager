package com.taskmanager.model

import java.util.Date

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val isCompleted: Boolean,
    val dueDate: Date?,
    val createdAt: Date,
    val updatedAt: Date,
    val userId: String,
    val isSynced: Boolean = false
)

enum class Priority(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

data class TaskFilter(
    val showCompleted: Boolean = true,
    val showActive: Boolean = true,
    val priority: Priority? = null,
    val sortBy: SortBy = SortBy.CREATED_DATE
)

enum class SortBy {
    CREATED_DATE,
    DUE_DATE,
    PRIORITY,
    TITLE
}
