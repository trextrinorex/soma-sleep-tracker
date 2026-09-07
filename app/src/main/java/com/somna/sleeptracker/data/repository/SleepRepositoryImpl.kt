package com.somna.sleeptracker.data.repository

import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.engine.SleepInferenceEngine
import com.somna.sleeptracker.domain.model.InferredSleepResult
import com.somna.sleeptracker.domain.model.TelemetryEvent
import com.somna.sleeptracker.domain.repository.SleepRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepositoryImpl @Inject constructor(
    private val dao: TelemetryDao,
    private val engine: SleepInferenceEngine
) : SleepRepository {

    override suspend fun logEvent(
        type: SystemEventType,
        timestamp: Long,
        metadata: String?
    ) {
        dao.insertEvent(
            EventLogEntity(
                eventType = type,
                timestamp = timestamp,
                metadataPayload = metadata
            )
        )
    }

    override suspend fun runInferenceForLastNight(): InferredSleepResult? {
        val now = System.currentTimeMillis()
        // Look back ~36 hours to capture overnight + possible shift
        val windowStart = now - TimeUnit.HOURS.toMillis(36)
        val events = dao.getEventsBetween(windowStart, now)
            .map { TelemetryEvent(it.eventType, it.timestamp, it.metadataPayload) }

        val historicalOnset = dao.getHistoricalMedianOnsetHour()
        val result = engine.processTelemetry(events, historicalOnset)

        if (result != null && result.confidence >= 0.55f) {
            dao.insertSleepSession(
                SleepSessionEntity(
                    startTime = result.startTime,
                    endTime = result.endTime,
                    durationMinutes = result.durationMinutes,
                    confidenceScore = result.confidence,
                    microAwakeningCount = result.microAwakeningCount,
                    featureBreakdownJson = result.featureBreakdown.entries
                        .joinToString(",") { "${it.key}=${it.value}" }
                )
            )
        }
        return result
    }

    override fun observeSessions(): Flow<List<SleepSessionEntity>> = dao.observeAllSessions()

    override suspend fun getRecentSessions(limit: Int): List<SleepSessionEntity> =
        dao.getRecentSessions(limit)

    override suspend fun getCircadianProfile(): CircadianProfileEntity? =
        dao.getCircadianProfile()

    override suspend fun updateCircadianProfile(profile: CircadianProfileEntity) {
        dao.upsertCircadianProfile(profile)
    }

    override suspend fun purgeOldTelemetry(olderThanDays: Int) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(olderThanDays.toLong())
        dao.purgeOldRawEvents(cutoff)
    }
}
