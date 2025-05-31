package com.example.raspisanieshgpu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.raspisanieshgpu.DataBase.MainDataBase
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleCheckWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val db by lazy { MainDataBase.getInstance(applicationContext) }
    private val gson = Gson()
    private val notificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override suspend fun doWork(): Result {
        Log.d("ScheduleCheckWorker", "Worker started")

        return try {
            withContext(Dispatchers.IO) {

//                showNotification(
//                    "test",
//                    "testtesttesttesttesttest",
//
//                )

                checkForScheduleChanges()
                Log.d("ScheduleCheckWorker", "Worker finished successfully")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("ScheduleCheckWorker", "Worker failed", e)
            Result.failure()
        }
    }

    private suspend fun checkForScheduleChanges() {
        Log.d("ScheduleCheckWorker", "Checking for schedule changes...")

        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val weekStart = getWeekStartDate(today).format(format)

        Log.d("ScheduleCheckWorker", "Week start: $weekStart")

        // Проверяем избранные группы
        checkGroupsSchedule(weekStart)

        // Проверяем избранных преподавателей
        checkTeachersSchedule(weekStart)
    }

    private suspend fun checkGroupsSchedule(weekStart: String) {
        val favoriteGroups = db.getGroupDao().getFavorites()

        for (group in favoriteGroups) {
            try {
                val cachedSchedule = group.scheduleData
                val apiSchedule = DataManager.fetchPairs(
                    weekStart,
                    1,
                    group.name,
                    "group",
                    applicationContext,
                )

                if (apiSchedule.ok && hasScheduleChanged(cachedSchedule, apiSchedule)) {
                    DataManager.saveCachedSchedule(
                        "group",
                        group.name,
                        apiSchedule,
                        applicationContext,
                    )
                    showNotification(
                        "Изменение расписания",
                        "Обнаружены изменения в расписании группы ${group.name}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ScheduleCheckWorker", "Error checking group ${group.name}", e)
            }
        }
    }

    private suspend fun checkTeachersSchedule(weekStart: String) {
        val favoriteTeachers = db.getTeacherDao().getFavorites()

        for (teacher in favoriteTeachers) {
            try {
                val cachedSchedule = teacher.scheduleData
                val apiSchedule = DataManager.fetchPairs(
                    weekStart,
                    1,
                    teacher.name,
                    "teacher",
                    applicationContext,
                )

                if (apiSchedule.ok && hasScheduleChanged(cachedSchedule, apiSchedule)) {
                    DataManager.saveCachedSchedule("teacher",
                        teacher.name,
                        apiSchedule,
                        applicationContext,
                    )
                    showNotification(
                        "Изменение расписания",
                        "Обнаружены изменения в расписании преподавателя ${teacher.name}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ScheduleCheckWorker", "Error checking teacher ${teacher.name}", e)
            }
        }
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    private fun hasScheduleChanged(cachedJson: String, newSchedule: PairsResponse): Boolean {
        Log.d("ScheduleCheckWorker", (cachedJson != Gson().toJson(newSchedule)).toString())

        return cachedJson != Gson().toJson(newSchedule)

    }

    private fun showNotification(title: String, message: String) {
        createNotificationChannelIfNeeded()

        val notificationId = (title + message).hashCode()

        val notification = NotificationCompat.Builder(applicationContext, "schedule_changes")
            .setSmallIcon(R.mipmap.logo_shspu)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (notificationManager.getNotificationChannel("schedule_changes") == null) {

            val channel = NotificationChannel(
                "schedule_changes",
                "Изменения расписания",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления об изменениях в расписании"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

}