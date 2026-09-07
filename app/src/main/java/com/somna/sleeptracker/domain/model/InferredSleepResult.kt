package com.somna.sleeptracker.domain.model

data class InferredSleepResult(
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val confidence: Float,
    val microAwakeningCount: Int,
    val featureBreakdown: Map<String, Float> = emptyMap()
)
