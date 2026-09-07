package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores running Bayesian estimates of the user's circadian profile.
 * Updated online via Welford's algorithm after each confirmed sleep session.
 */
@Entity(tableName = "circadian_profile")
data class CircadianProfileEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val meanOnsetHour: Float = 23.5f,      // Running mean of sleep onset hour
    val varianceOnset: Float = 1.5f,       // Running variance
    val meanDurationHours: Float = 7.5f,
    val varianceDuration: Float = 1.0f,
    val sampleCount: Int = 0,
    val workdayMeanOnset: Float = 23.2f,   // Clustered workday onset
    val freeDayMeanOnset: Float = 24.1f,   // Clustered free-day onset
    val socialJetlagHours: Float = 0.9f,   // |free - work|
    val lastUpdated: Long = 0L
)
