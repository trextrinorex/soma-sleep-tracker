package com.somna.sleeptracker.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleAll() {
        scheduleMorningInference()
        scheduleDataRetention()
    }

    private fun scheduleMorningInference() {
        val request = PeriodicWorkRequestBuilder<MorningBatchInferenceWorker>(
            12, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "morning_batch_inference",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleDataRetention() {
        val request = PeriodicWorkRequestBuilder<DataRetentionWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "data_retention",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
