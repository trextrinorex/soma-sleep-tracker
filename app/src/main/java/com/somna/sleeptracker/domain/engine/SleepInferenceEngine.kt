package com.somna.sleeptracker.domain.engine

import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.model.InferredSleepResult
import com.somna.sleeptracker.domain.model.RawGap
import com.somna.sleeptracker.domain.model.TelemetryEvent
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow

/**
 * Core on-device sleep inference engine.
 * Converts discrete telemetry into high-probability sleep sessions using
 * a multi-variate logistic (sigmoid) model + micro-awakening absorption.
 *
 * Pipeline (Phase 2):
 * 1. Serialize & sanitize events
 * 2. Extract gaps & merge micro-awakenings (<=180s, max 4)
 * 3. Score candidates with 5 features (Circadian, Duration, Charging, Entropy, History)
 * 4. Apply sigmoid confidence and emit best result
 */
@Singleton
class SleepInferenceEngine @Inject constructor() {

    companion object {
        private const val MIN_GAP_MINUTES = 210L // 3.5 hours minimum
        private const val MAX_MICRO_AWAKENING_MS = 180_000L // 3 minutes
        private const val MAX_MERGES_ALLOWED = 4

        // Logistic regression hyperparameters (from PDS Phase 2)
        private const val W_INTERCEPT = -2.85f
        private const val W_CIRCADIAN = 2.40f
        private const val W_DURATION = 2.10f
        private const val W_CHARGING = 1.65f
        private const val W_ENTROPY = 1.10f
        private const val W_HISTORY = 1.80f

        // Circadian Gaussian peak (typical nocturnal onset ~23:30)
        private const val CIRCADIAN_PEAK_HOUR = 23.5f
        private const val CIRCADIAN_SIGMA = 2.2f
    }

    fun processTelemetry(
        events: List<TelemetryEvent>,
        historicalMedianOnsetHour: Float? = null
    ): InferredSleepResult? {
        if (events.isEmpty()) return null

        val sortedEvents = events.sortedBy { it.timestamp }
        val gaps = extractGaps(sortedEvents)
        val mergedGaps = mergeMicroAwakenings(gaps)

        val minGapMs = TimeUnit.MINUTES.toMillis(MIN_GAP_MINUTES)
        val validCandidates = mergedGaps.filter { it.first.durationMillis >= minGapMs }

        if (validCandidates.isEmpty()) return null

        return validCandidates
            .map { (gap, microCount) ->
                scoreCandidate(gap, microCount, sortedEvents, historicalMedianOnsetHour)
            }
            .maxByOrNull { it.confidence }
    }

    private fun extractGaps(events: List<TelemetryEvent>): List<RawGap> {
        val gaps = mutableListOf<RawGap>()
        var lastOffTime: Long? = null
        var lastAppCategory: String? = null

        for (event in events) {
            when (event.type) {
                SystemEventType.APP_CATEGORY_ACCESSED -> {
                    lastAppCategory = event.metadata
                }
                SystemEventType.SCREEN_OFF -> {
                    lastOffTime = event.timestamp
                }
                SystemEventType.SCREEN_ON, SystemEventType.USER_PRESENT -> {
                    if (lastOffTime != null && event.timestamp > lastOffTime) {
                        gaps.add(
                            RawGap(
                                startTime = lastOffTime,
                                endTime = event.timestamp,
                                preSleepCategory = lastAppCategory
                            )
                        )
                        lastOffTime = null
                    }
                }
                else -> { /* Power events scored separately */ }
            }
        }
        return gaps
    }

    private fun mergeMicroAwakenings(gaps: List<RawGap>): List<Pair<RawGap, Int>> {
        if (gaps.isEmpty()) return emptyList()

        val results = mutableListOf<Pair<RawGap, Int>>()
        var currentGap = gaps[0]
        var microCount = 0

        for (i in 1 until gaps.size) {
            val nextGap = gaps[i]
            val wakeInterval = nextGap.startTime - currentGap.endTime

            if (wakeInterval <= MAX_MICRO_AWAKENING_MS && microCount < MAX_MERGES_ALLOWED) {
                // Absorb short nocturnal check into continuous sleep window
                currentGap = RawGap(
                    startTime = currentGap.startTime,
                    endTime = nextGap.endTime,
                    preSleepCategory = currentGap.preSleepCategory
                )
                microCount++
            } else {
                results.add(currentGap to microCount)
                currentGap = nextGap
                microCount = 0
            }
        }
        results.add(currentGap to microCount)
        return results
    }

    private fun scoreCandidate(
        gap: RawGap,
        microCount: Int,
        allEvents: List<TelemetryEvent>,
        historicalMedianOnsetHour: Float?
    ): InferredSleepResult {
        val onsetHour = getDecimalHour(gap.startTime)
        val durationHours = gap.durationMillis / 3_600_000f

        // 1. Circadian Gaussian (peaks around 23:30)
        val circadianDelta = min(
            kotlin.math.abs(onsetHour - CIRCADIAN_PEAK_HOUR),
            24f - kotlin.math.abs(onsetHour - CIRCADIAN_PEAK_HOUR)
        )
        val fCircadian = exp(-(circadianDelta.pow(2)) / (2f * CIRCADIAN_SIGMA.pow(2)))

        // 2. Duration Plausibility (Gaussian centered at 7.5h)
        val fDuration = if (durationHours < 3.5f) {
            0.0f
        } else {
            exp(-((durationHours - 7.5f).pow(2)) / (2f * 1.6f.pow(2)))
        }

        // 3. Charging Correlation
        val fCharging = evaluateChargingCorrelation(gap, allEvents)

        // 4. Pre-sleep Entropy (app category)
        val fEntropy = when (gap.preSleepCategory) {
            "SOCIAL", "MEDIA_VIDEO", "GAMING" -> 1.0f
            "COMMUNICATION" -> 0.85f
            "PRODUCTIVITY", "WORK", "BUSINESS" -> 0.40f
            else -> 0.50f
        }

        // 5. Historical Consistency
        val fHistory = if (historicalMedianOnsetHour != null) {
            val deltaHist = min(
                kotlin.math.abs(onsetHour - historicalMedianOnsetHour),
                24f - kotlin.math.abs(onsetHour - historicalMedianOnsetHour)
            )
            exp(-(deltaHist.pow(2)) / (2f * 1.2f.pow(2)))
        } else {
            0.50f // Cold-start neutral
        }

        // Linear logit
        val z = W_INTERCEPT +
                (W_CIRCADIAN * fCircadian) +
                (W_DURATION * fDuration) +
                (W_CHARGING * fCharging) +
                (W_ENTROPY * fEntropy) +
                (W_HISTORY * fHistory)

        // Sigmoid activation
        val confidence = (1.0f / (1.0f + exp(-z))).coerceIn(0.0f, 1.0f)
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(gap.durationMillis).toInt()

        return InferredSleepResult(
            startTime = gap.startTime,
            endTime = gap.endTime,
            durationMinutes = durationMinutes,
            confidence = confidence,
            microAwakeningCount = microCount,
            featureBreakdown = mapOf(
                "f_circadian" to fCircadian,
                "f_duration" to fDuration,
                "f_charging" to fCharging,
                "f_entropy" to fEntropy,
                "f_history" to fHistory
            )
        )
    }

    private fun evaluateChargingCorrelation(
        gap: RawGap,
        allEvents: List<TelemetryEvent>
    ): Float {
        val windowMarginMs = TimeUnit.MINUTES.toMillis(20)
        var attachedNearStart = false
        var stayedCharging = false

        for (event in allEvents) {
            when (event.type) {
                SystemEventType.POWER_CONNECTED_AC,
                SystemEventType.POWER_CONNECTED_WIRELESS -> {
                    if (kotlin.math.abs(event.timestamp - gap.startTime) <= windowMarginMs) {
                        attachedNearStart = true
                    }
                    // Check if still connected during the gap
                    if (event.timestamp in gap.startTime..gap.endTime) {
                        stayedCharging = true
                    }
                }
                SystemEventType.POWER_DISCONNECTED -> {
                    if (kotlin.math.abs(event.timestamp - gap.endTime) <= windowMarginMs) {
                        // Detached near wake is also a positive signal
                        stayedCharging = true
                    }
                }
                else -> {}
            }
        }

        return when {
            attachedNearStart && stayedCharging -> 1.0f
            attachedNearStart || stayedCharging -> 0.7f
            else -> 0.25f
        }
    }

    private fun getDecimalHour(epochMs: Long): Float {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        return cal.get(Calendar.HOUR_OF_DAY) +
                cal.get(Calendar.MINUTE) / 60f +
                cal.get(Calendar.SECOND) / 3600f
    }
}
