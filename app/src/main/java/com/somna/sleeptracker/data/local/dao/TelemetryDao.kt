package com.somna.sleeptracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    // ---------- Telemetry Events ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<EventLogEntity>)

    @Query("SELECT * FROM raw_telemetry_events WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getEventsBetween(start: Long, end: Long): List<EventLogEntity>

    @Query("SELECT * FROM raw_telemetry_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<EventLogEntity>

    @Query("DELETE FROM raw_telemetry_events WHERE timestamp < :cutoff")
    suspend fun purgeOldRawEvents(cutoff: Long): Int

    @Query("SELECT COUNT(*) FROM raw_telemetry_events")
    suspend fun getEventCount(): Int

    // ---------- Sleep Sessions ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity): Long

    @Update
    suspend fun updateSleepSession(session: SleepSessionEntity)

    @Query("SELECT * FROM inferred_sleep_sessions ORDER BY startTime DESC")
    fun observeAllSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM inferred_sleep_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<SleepSessionEntity>

    @Query("SELECT * FROM inferred_sleep_sessions WHERE startTime BETWEEN :start AND :end ORDER BY startTime ASC")
    suspend fun getSessionsBetween(start: Long, end: Long): List<SleepSessionEntity>

    @Query("SELECT * FROM inferred_sleep_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SleepSessionEntity?

    @Query("SELECT AVG((startTime % 86400000) / 3600000.0) FROM inferred_sleep_sessions WHERE confidenceScore >= 0.6")
    suspend fun getHistoricalMedianOnsetHour(): Float?

    // ---------- Circadian Profile ----------
    @Query("SELECT * FROM circadian_profile WHERE id = 1")
    suspend fun getCircadianProfile(): CircadianProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCircadianProfile(profile: CircadianProfileEntity)
}
