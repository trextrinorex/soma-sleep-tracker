package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "raw_telemetry_events",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["eventType", "timestamp"])
    ]
)
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: SystemEventType,
    val timestamp: Long,           // Epoch Milliseconds
    val metadataPayload: String? = null // Optional metadata (e.g. app category)
)
