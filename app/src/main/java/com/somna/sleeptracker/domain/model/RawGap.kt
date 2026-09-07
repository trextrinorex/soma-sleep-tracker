package com.somna.sleeptracker.domain.model

data class RawGap(
    val startTime: Long,
    val endTime: Long,
    val preSleepCategory: String? = null
) {
    val durationMillis: Long
        get() = endTime - startTime
}
