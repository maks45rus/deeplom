package com.example.raspisanieshgpu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.raspisanieshgpu.Data.DataBase.Group
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import com.example.raspisanieshgpu.Data.DataBase.SavedOther
import com.example.raspisanieshgpu.Data.DataBase.Teacher
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.Data.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleCheckWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val db by lazy { MainDataBase.getInstance(applicationContext) }
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

        // Проверяем избранные группы
        checkFavoritesSchedule(
            weekStart = weekStart,
            type = "group",
            getName = { (it as Group).name },
            getFavorites = { db.getGroupDao().getFavorites() },
            getCachedSchedule = { (it as Group).scheduleData },
            getWeekStartDate = { (it as Group).weekStartDate }
        )

        // Проверяем избранных преподавателей
        checkFavoritesSchedule(
            weekStart = weekStart,
            type = "teacher",
            getName = { (it as Teacher).name },
            getFavorites = { db.getTeacherDao().getFavorites() },
            getCachedSchedule = { (it as Teacher).scheduleData },
            getWeekStartDate = { (it as Teacher).weekStartDate }
        )

        // Проверяем другие избранные элементы
        checkFavoritesSchedule(
            weekStart = weekStart,
            type = "other",
            getName = { (it as SavedOther).name },
            getFavorites = { db.getSavedOtherDao().getAllSavedOther() },
            getCachedSchedule = { (it as SavedOther).scheduleData },
            getWeekStartDate = { (it as SavedOther).weekStartDate }
        )

    }

    private suspend fun checkFavoritesSchedule(
        weekStart: String,
        type: String,
        getName: (Any) -> String,
        getFavorites: suspend () -> List<Any>,
        getCachedSchedule: (Any) -> String,
        getWeekStartDate: (Any) -> String
    ) {
        val favorites = getFavorites()

        for (item in favorites) {
            try {
                val cachedSchedule = getCachedSchedule(item)
                val apiSchedule = DataManager.fetchPairs(
                    weekStart,
                    1,
                    getName(item),
                    type,
                    applicationContext
                )

                if (weekStart == getWeekStartDate(item) &&
                    apiSchedule.ok &&
                    hasScheduleChanged(cachedSchedule, apiSchedule)) {

                    showNotification(
                        applicationContext.getString(R.string.schedule_change_title),
                        applicationContext.getString(
                            R.string.schedule_change_message,
                            type,
                            getName(item)
                        )
                    )
                }

                DataManager.saveCachedSchedule(
                    type,
                    getName(item),
                    apiSchedule,
                    weekStart,
                    applicationContext
                )
            } catch (e: Exception) {
                Log.e("ScheduleCheckWorker", "Error checking $type ${getName(item)}", e)
            }
        }
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    private fun hasScheduleChanged(cachedJson: String, newSchedule: PairsResponse): Boolean {
        if(cachedJson == ""){
            return false
        }
        return cachedJson != Gson().toJson(newSchedule)
//      return true
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
                applicationContext.getString(R.string.schedule_change_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления об изменениях в расписании"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

}