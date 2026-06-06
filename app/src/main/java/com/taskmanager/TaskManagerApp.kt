package com.taskmanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

//a comment bcs i need more than 1 commit for some reason
@HiltAndroidApp
class TaskManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("settings", 0)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        createNotificationChannels()
    }
    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("settings", 0)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = base.resources.configuration
        config.setLocale(locale)
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
                ?: return

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                getString(R.string.channel_tasks_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_tasks_desc)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_reminders_desc)
            }

            notificationManager.createNotificationChannels(listOf(taskChannel, reminderChannel))
        }
    }

    companion object {
        const val CHANNEL_TASKS = "tasks_channel"
        const val CHANNEL_REMINDERS = "reminders_channel"
        private const val TAG = "TaskManagerApp"
    }
}
