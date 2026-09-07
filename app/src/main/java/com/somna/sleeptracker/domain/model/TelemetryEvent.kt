package com.somna.sleeptracker.domain.model

import com.somna.sleeptracker.data.local.entity.SystemEventType

/**
 * Domain representation of a raw telemetry event.
 * Maps cleanly from EventLogEntity.
 */
data class TelemetryEvent(
    val type: SystemEventType,
    val timestamp: Long,
    val metadata: String? = null
)
