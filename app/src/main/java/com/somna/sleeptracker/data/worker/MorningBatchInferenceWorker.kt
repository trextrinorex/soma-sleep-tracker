package com.somna.sleeptracker.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.somna.sleeptracker.domain.repository.SleepRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MorningBatchInferenceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SleepRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.runInferenceForLastNight()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
