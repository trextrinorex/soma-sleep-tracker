package com.somna.sleeptracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.somna.sleeptracker.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "Somna",
            color = SoftWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Zero-Wearable Sleep Engine",
            color = MutedGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoAccent)
            }
        } else {
            state.lastNight?.let { night ->
                LastNightCard(night)
                Spacer(modifier = Modifier.height(16.dp))
            } ?: run {
                Text(
                    text = "No sleep session inferred yet.\nKeep the app running overnight.",
                    color = MutedGray,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.socialJetlagHours?.let {
                Text(
                    text = "Social Jetlag: ${String.format("%.1f", it)} h",
                    color = if (it > 1.5f) WarningAmber else SuccessGreen,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onEvent(DashboardEvent.RunManualInference) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Inference Now", color = SoftWhite)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.onEvent(DashboardEvent.Refresh) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh", color = IndigoLight)
            }

            if (state.weeklyTrend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Recent Trend", color = SoftWhite, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                state.weeklyTrend.forEach { day ->
                    Text(
                        text = "${day.dayLabel}: ${String.format("%.1f", day.durationHours)}h  (conf ${ (day.confidence * 100).toInt() }%)",
                        color = MutedGray,
                        fontSize = 13.sp
                    )
                }
            }

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = ErrorRose, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LastNightCard(night: SleepMetricUiModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Last Night", color = MutedGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.1f h", night.durationHours),
                color = SoftWhite,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${night.onsetLabel} → ${night.wakeLabel}",
                color = IndigoLight,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip(
                    label = "Confidence",
                    value = "${night.confidencePercent}%",
                    color = if (night.isHighConfidence) SuccessGreen else WarningAmber
                )
                MetricChip(
                    label = "Micro-wakes",
                    value = "${night.microAwakenings}",
                    color = MutedGray
                )
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Column {
        Text(label, color = MutedGray, fontSize = 11.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
