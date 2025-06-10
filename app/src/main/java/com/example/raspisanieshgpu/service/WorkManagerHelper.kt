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
            30, TimeUnit.MINUTES,
        ).setConstraints(constraints).build()


        workManager.apply {
            cancelUniqueWork("check_work")
            enqueueUniquePeriodicWork(
                "check_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest,
            )
        }


    }
}