package com.example.raspisanieshgpu.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkManagerHelper(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun setupScheduleCheckWorker() {
        Log.d("WorkManagerHelper", "Initializing worker...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ScheduleCheckWorker>(
            1, TimeUnit.HOURS,
            5, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<ScheduleCheckWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        workManager.apply {
            cancelUniqueWork("check_work")
            enqueueUniquePeriodicWork(
                "check_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
            enqueue(oneTimeRequest)
        }

        workManager.apply {
            cancelUniqueWork("schedule_check_work")
            enqueueUniquePeriodicWork(
                "schedule_check_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
            enqueue(oneTimeRequest)
        }
    }
}