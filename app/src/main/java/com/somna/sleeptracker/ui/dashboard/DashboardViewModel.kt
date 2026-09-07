package com.somna.sleeptracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SleepRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        loadData()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Refresh -> loadData()
            DashboardEvent.RunManualInference -> runInference()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val sessions = repository.getRecentSessions(14)
                val profile = repository.getCircadianProfile()

                val lastNight = sessions.firstOrNull()?.toUiModel()
                val trend = sessions.take(7).reversed().mapIndexed { idx, s ->
                    DayTrendUiModel(
                        dayLabel = "D${idx + 1}",
                        durationHours = s.durationMinutes / 60f,
                        confidence = s.confidenceScore
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        lastNight = lastNight,
                        weeklyTrend = trend,
                        socialJetlagHours = profile?.socialJetlagHours,
                        meanOnsetLabel = profile?.let {
                            String.format(Locale.US, "%.1fh", it.meanOnsetHour)
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun runInference() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.runInferenceForLastNight()
            loadData()
        }
    }

    private fun SleepSessionEntity.toUiModel(): SleepMetricUiModel {
        return SleepMetricUiModel(
            durationHours = durationMinutes / 60f,
            confidencePercent = (confidenceScore * 100).toInt(),
            microAwakenings = microAwakeningCount,
            onsetLabel = timeFormat.format(Date(startTime)),
            wakeLabel = timeFormat.format(Date(endTime)),
            isHighConfidence = confidenceScore >= 0.7f
        )
    }
}
