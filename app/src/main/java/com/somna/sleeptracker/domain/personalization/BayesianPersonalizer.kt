package com.somna.sleeptracker.domain.personalization

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.domain.model.InferredSleepResult
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Online Bayesian-style personalization using Welford's algorithm for
 * running mean/variance of onset hour and duration.
 * Also maintains simple workday / free-day clusters and social jetlag.
 */
@Singleton
class BayesianPersonalizer @Inject constructor() {

    fun updateProfile(
        current: CircadianProfileEntity?,
        result: InferredSleepResult,
        isWorkday: Boolean
    ): CircadianProfileEntity {
        val onsetHour = getDecimalHour(result.startTime)
        val durationH = result.durationMinutes / 60f

        val prev = current ?: CircadianProfileEntity()
        val n = prev.sampleCount + 1

        // Welford online mean & variance for onset
        val deltaOnset = onsetHour - prev.meanOnsetHour
        val newMeanOnset = prev.meanOnsetHour + deltaOnset / n
        val delta2Onset = onsetHour - newMeanOnset
        val newVarOnset = if (n > 1) {
            ((n - 2) * prev.varianceOnset + deltaOnset * delta2Onset) / (n - 1)
        } else prev.varianceOnset

        // Duration
        val deltaDur = durationH - prev.meanDurationHours
        val newMeanDur = prev.meanDurationHours + deltaDur / n
        val delta2Dur = durationH - newMeanDur
        val newVarDur = if (n > 1) {
            ((n - 2) * prev.varianceDuration + deltaDur * delta2Dur) / (n - 1)
        } else prev.varianceDuration

        // Simple cluster update
        val (newWork, newFree) = if (isWorkday) {
            val alpha = 0.15f
            (prev.workdayMeanOnset * (1 - alpha) + onsetHour * alpha) to prev.freeDayMeanOnset
        } else {
            val alpha = 0.15f
            prev.workdayMeanOnset to (prev.freeDayMeanOnset * (1 - alpha) + onsetHour * alpha)
        }

        val jetlag = abs(newFree - newWork)

        return CircadianProfileEntity(
            id = 1,
            meanOnsetHour = newMeanOnset,
            varianceOnset = newVarOnset.coerceAtLeast(0.1f),
            meanDurationHours = newMeanDur,
            varianceDuration = newVarDur.coerceAtLeast(0.1f),
            sampleCount = n,
            workdayMeanOnset = newWork,
            freeDayMeanOnset = newFree,
            socialJetlagHours = jetlag,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun getDecimalHour(epochMs: Long): Float {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        return cal.get(Calendar.HOUR_OF_DAY) +
                cal.get(Calendar.MINUTE) / 60f
    }
}
