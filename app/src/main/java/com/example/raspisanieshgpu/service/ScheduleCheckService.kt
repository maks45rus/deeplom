package com.example.raspisanieshgpu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.raspisanieshgpu.R

class ScheduleCheckService : Service() {
    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        WorkManager
        // Запускаем проверку
        WorkManager.getInstance(this)
            .beginWith(OneTimeWorkRequestBuilder<ScheduleCheckWorker>().build())
            .enqueue()

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            "schedule_check_channel",
            "Schedule Updates",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Checks for schedule changes"
        }

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        return NotificationCompat.Builder(this, "schedule_check_channel")
            .setContentTitle("Schedule Check")
            .setContentText("Checking for schedule updates...")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()
    }
}