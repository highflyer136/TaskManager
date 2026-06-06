package com.taskmanager.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.taskmanager.model.Priority
import com.taskmanager.model.Task
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userTasksCollection(userId: String) =
        firestore.collection("users").document(userId).collection("tasks")

    suspend fun syncTask(task: Task) {
        val data = mapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "priority" to task.priority.value,
            "isCompleted" to task.isCompleted,
            "dueDate" to task.dueDate?.time,
            "createdAt" to task.createdAt.time,
            "updatedAt" to task.updatedAt.time,
            "isDeleted" to false
        )
        userTasksCollection(task.userId)
            .document(task.id)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        userTasksCollection(userId)
            .document(taskId)
            .update(mapOf("isDeleted" to true, "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun fetchAllTasks(userId: String): List<Task> {
        val snapshot = userTasksCollection(userId)
            .whereEqualTo("isDeleted", false)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null
                Task(
                    id = data["id"] as? String ?: doc.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    priority = Priority.fromInt((data["priority"] as? Long)?.toInt() ?: 1),
                    isCompleted = data["isCompleted"] as? Boolean ?: false,
                    dueDate = (data["dueDate"] as? Long)?.let { Date(it) },
                    createdAt = Date((data["createdAt"] as? Long) ?: System.currentTimeMillis()),
                    updatedAt = Date((data["updatedAt"] as? Long) ?: System.currentTimeMillis()),
                    userId = userId,
                    isSynced = true
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun syncBatch(tasks: List<Task>) {
        if (tasks.isEmpty()) return
        val batch = firestore.batch()
        tasks.forEach { task ->
            val docRef = userTasksCollection(task.userId).document(task.id)
            val data = mapOf(
                "id" to task.id,
                "title" to task.title,
                "description" to task.description,
                "priority" to task.priority.value,
                "isCompleted" to task.isCompleted,
                "dueDate" to task.dueDate?.time,
                "createdAt" to task.createdAt.time,
                "updatedAt" to task.updatedAt.time,
                "isDeleted" to false
            )
            batch.set(docRef, data, SetOptions.merge())
        }
        batch.commit().await()
    }

}
