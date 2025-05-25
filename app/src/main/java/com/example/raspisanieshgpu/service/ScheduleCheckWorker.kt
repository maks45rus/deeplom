package com.example.raspisanieshgpu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleCheckWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ScheduleCheckWorker", "Worker started")
        return withContext(Dispatchers.IO) {
            try {
                checkForScheduleChanges()
                Log.d("ScheduleCheckWorker", "Worker finished successfully")
                Result.success()
            } catch (e: Exception) {
                Log.e("ScheduleCheckWorker", "Worker failed", e)
                Result.failure()
            }
        }
    }

    private suspend fun checkForScheduleChanges() {
        showNotification("Тест", "Worker работает! Проверка расписания...")
        Log.d("ScheduleCheckWorker", "Checking for schedule changes...")
        val db = databaseobj.database
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val weekStart = getWeekStartDate(today).format(format)

        Log.d("ScheduleCheckWorker", "Week start: $weekStart")
        // Проверяем избранные группы
        val favoriteGroups = db.getGroupDao().getFavorites()
        for (group in favoriteGroups) {
            val cachedSchedule = db.getCachedScheduleDao().getSchedule(
                group.id,
                "group",
                getWeekStartDate(today).format(format)
            )

            if (cachedSchedule != null) {
                val apiSchedule = DataManager.fetchPairs(
                    getWeekStartDate(today).format(format),
                    1,
                    group.id,
                    "group"
                )

                Log.d("WorkerNotif", "group ${(apiSchedule.ok &&
                        hasScheduleChanged(cachedSchedule.scheduleData, apiSchedule))}")
                if (apiSchedule.ok && hasScheduleChanged(cachedSchedule.scheduleData, apiSchedule)) {
                    showNotification(
                        "Изменение расписания",
                        "Обнаружены изменения в расписании группы ${group.name}"
                    )
                }
            }
        }

        // Проверяем избранных преподавателей
        val favoriteTeachers = db.getTeacherDao().getFavorites()
        for (teacher in favoriteTeachers) {
            val cachedSchedule = db.getCachedScheduleDao().getSchedule(
                teacher.id,
                "teacher",
                getWeekStartDate(today).format(format)
            )

            if (cachedSchedule != null) {
                val apiSchedule = DataManager.fetchPairs(
                    getWeekStartDate(today).format(format),
                    1,
                    teacher.id,
                    "teacher"
                )

                Log.d("WorkerNotif", "prepod ${(apiSchedule.ok &&
                        hasScheduleChanged(cachedSchedule.scheduleData, apiSchedule))}")
                if (apiSchedule.ok && hasScheduleChanged(cachedSchedule.scheduleData, apiSchedule)) {
                    showNotification(
                        "Изменение расписания",
                        "Обнаружены изменения в расписании преподавателя ${teacher.name}"
                    )
                }
            }
        }
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    private fun hasScheduleChanged(cachedJson: String, newSchedule: PairsResponse): Boolean {
        // Здесь нужно сравнить кэшированное расписание с новым
        // Можно использовать Gson для преобразования и сравнения
        // Вернуть true, если есть различия
        return true // Заглушка - реализуйте реальное сравнение
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Создаем канал уведомлений (для Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "schedule_changes",
                "Изменения расписания",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "schedule_changes")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}