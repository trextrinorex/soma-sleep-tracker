package com.somna.sleeptracker.domain.resilience

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guards against NTP time jumps and OEM clock skew.
 * Prefers monotonic elapsedRealtime when wall-clock delta is suspicious.
 */
@Singleton
class TimeAnchorValidator @Inject constructor() {

    data class SafeTimePair(
        val wallClockMs: Long,
        val monotonicMs: Long,
        val usedMonotonic: Boolean
    )

    fun calculateTrueDeltaMs(
        previousWall: Long,
        previousMono: Long,
        currentWall: Long = System.currentTimeMillis(),
        currentMono: Long = SystemClock.elapsedRealtime()
    ): SafeTimePair {
        val wallDelta = currentWall - previousWall
        val monoDelta = currentMono - previousMono

        // If discrepancy > 60 seconds, trust monotonic
        val discrepancy = kotlin.math.abs(wallDelta - monoDelta)
        val useMono = discrepancy > 60_000L

        return SafeTimePair(
            wallClockMs = if (useMono) previousWall + monoDelta else currentWall,
            monotonicMs = currentMono,
            usedMonotonic = useMono
        )
    }
}
