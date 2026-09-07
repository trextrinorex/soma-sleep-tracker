package com.somna.sleeptracker.domain.repository

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.model.InferredSleepResult
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    suspend fun logEvent(type: SystemEventType, timestamp: Long = System.currentTimeMillis(), metadata: String? = null)
    suspend fun runInferenceForLastNight(): InferredSleepResult?
    fun observeSessions(): Flow<List<SleepSessionEntity>>
    suspend fun getRecentSessions(limit: Int = 14): List<SleepSessionEntity>
    suspend fun getCircadianProfile(): CircadianProfileEntity?
    suspend fun updateCircadianProfile(profile: CircadianProfileEntity)
    suspend fun purgeOldTelemetry(olderThanDays: Int = 14)
}
