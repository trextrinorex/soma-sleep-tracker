package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inferred_sleep_sessions",
    indices = [Index(value = ["startTime", "endTime"])]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,         // Inferred onset (Epoch ms)
    val endTime: Long,           // Inferred wake (Epoch ms)
    val durationMinutes: Int,
    val confidenceScore: Float,  // Value between 0.00 and 1.00
    val microAwakeningCount: Int, // Number of <=3 min phone checks merged
    val isUserEdited: Boolean = false,
    val userCorrectionDelta: Long = 0L, // Tracks user adjustment offset in milliseconds
    val featureBreakdownJson: String? = null // Optional JSON of feature scores for debugging
)
