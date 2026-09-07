package com.somna.sleeptracker.ui.dashboard

data class SleepMetricUiModel(
    val durationHours: Float,
    val confidencePercent: Int,
    val microAwakenings: Int,
    val onsetLabel: String,
    val wakeLabel: String,
    val isHighConfidence: Boolean
)

data class DayTrendUiModel(
    val dayLabel: String,
    val durationHours: Float,
    val confidence: Float
)

data class DashboardState(
    val isLoading: Boolean = true,
    val lastNight: SleepMetricUiModel? = null,
    val weeklyTrend: List<DayTrendUiModel> = emptyList(),
    val socialJetlagHours: Float? = null,
    val meanOnsetLabel: String? = null,
    val errorMessage: String? = null
)

sealed interface DashboardEvent {
    data object Refresh : DashboardEvent
    data object RunManualInference : DashboardEvent
}
