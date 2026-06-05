package com.taskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.taskmanager.data.local.dao.TaskDao
import com.taskmanager.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "task_manager_db"
    }
}
