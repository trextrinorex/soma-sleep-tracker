package com.somna.sleeptracker.domain.engine

import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SleepInferenceEngineTest {

    private lateinit var engine: SleepInferenceEngine

    @Before
    fun setup() {
        engine = SleepInferenceEngine()
    }

    @Test
    fun standardNocturnalSession_returnsHighConfidence() {
        // Simulate a clean night: screen off 23:30, screen on 07:00 next day
        val base = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12)
        val tOff = base // ~23:30 equivalent for test
        val tOn = base + TimeUnit.HOURS.toMillis(7) + TimeUnit.MINUTES.toMillis(30)

        val events = listOf(
            TelemetryEvent(SystemEventType.APP_CATEGORY_ACCESSED, tOff - 5_000, "SOCIAL"),
            TelemetryEvent(SystemEventType.SCREEN_OFF, tOff),
            TelemetryEvent(SystemEventType.POWER_CONNECTED_AC, tOff + 2_000),
            TelemetryEvent(SystemEventType.SCREEN_ON, tOn),
            TelemetryEvent(SystemEventType.USER_PRESENT, tOn + 1_000)
        )

        val result = engine.processTelemetry(events, historicalMedianOnsetHour = 23.5f)

        assertNotNull(result)
        assertTrue("Confidence should be high for clean nocturnal session", result!!.confidence > 0.65f)
        assertEquals(0, result.microAwakeningCount)
        assertTrue(result.durationMinutes in 420..480) // ~7–8h
    }

    @Test
    fun midnightMicroAwakening_isAbsorbedCorrectly() {
        val base = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10)
        val tOff1 = base
        val tOn1 = base + TimeUnit.HOURS.toMillis(3)          // short wake
        val tOff2 = tOn1 + 90_000                             // 1.5 min check
        val tOn2 = tOff2 + TimeUnit.HOURS.toMillis(4) + TimeUnit.MINUTES.toMillis(30)

        val events = listOf(
            TelemetryEvent(SystemEventType.SCREEN_OFF, tOff1),
            TelemetryEvent(SystemEventType.SCREEN_ON, tOn1),
            TelemetryEvent(SystemEventType.USER_PRESENT, tOn1 + 500),
            TelemetryEvent(SystemEventType.SCREEN_OFF, tOff2),
            TelemetryEvent(SystemEventType.SCREEN_ON, tOn2)
        )

        val result = engine.processTelemetry(events)

        assertNotNull(result)
        assertTrue("Micro-awakening should be absorbed", result!!.microAwakeningCount >= 1)
        assertTrue("Merged duration should exceed 7h", result.durationMinutes > 420)
    }

    @Test
    fun shortGap_belowThreshold_returnsNull() {
        val base = System.currentTimeMillis()
        val events = listOf(
            TelemetryEvent(SystemEventType.SCREEN_OFF, base),
            TelemetryEvent(SystemEventType.SCREEN_ON, base + TimeUnit.HOURS.toMillis(2))
        )

        val result = engine.processTelemetry(events)
        assertNull("Gaps shorter than 3.5h must be rejected", result)
    }

    @Test
    fun emptyEvents_returnsNull() {
        assertNull(engine.processTelemetry(emptyList()))
    }
}
