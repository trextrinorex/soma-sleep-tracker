package com.somna.sleeptracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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

    Surface(color = DeepBlack, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { TrackingBanner() }
            item {
                if (state.isLoading) LoadingCard()
                else if (state.lastNight != null) LastNightCard(state.lastNight!!)
                else EmptySleepCard()
            }
            if (!state.isLoading && state.lastNight != null) {
                item { InsightRow(state.lastNight!!) }
            }
            state.socialJetlagHours?.let { hours -> item { SocialJetlagCard(hours) } }
            if (state.weeklyTrend.isNotEmpty()) {
                item { SectionTitle("7-day sleep trend", "Your recent sleep duration") }
                item { TrendCard(state.weeklyTrend) }
            }
            item {
                ActionCard(
                    onInference = { viewModel.onEvent(DashboardEvent.RunManualInference) },
                    onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) },
                    loading = state.isLoading
                )
            }
            state.errorMessage?.let { message -> item { ErrorCard(message) } }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("SOMNA", color = SoftWhite, fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(3.dp))
            Text("Sleep, understood.", color = MutedGray, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier.size(42.dp).background(CardSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("S", color = IndigoLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TrackingBanner() {
    Surface(shape = RoundedCornerShape(14.dp), color = CardSurface, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(SuccessGreen, CircleShape))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Sleep tracking is ready", color = SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Keep Somna running overnight for the best result.", color = MutedGray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = IndigoAccent, strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun EmptySleepCard() {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("LAST NIGHT", color = MutedGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
            Text("Your first night starts here", color = SoftWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(
                "Somna needs an overnight session before it can estimate your sleep. Once data is available, your duration, confidence and sleep insights will appear here.",
                color = MutedGray, fontSize = 14.sp, lineHeight = 21.sp
            )
            Surface(shape = RoundedCornerShape(12.dp), color = SurfaceDark, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("01", color = IndigoLight, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text("Use your phone normally, then leave Somna running overnight.", color = SoftWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LastNightCard(night: SleepMetricUiModel) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("LAST NIGHT", color = MutedGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(String.format("%.1f", night.durationHours), color = SoftWhite, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("hours asleep", color = MutedGray, fontSize = 13.sp)
                }
                ConfidenceBadge(night.confidencePercent, night.isHighConfidence)
            }
            Spacer(Modifier.height(18.dp))
            Text("SLEEP WINDOW", color = MutedGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(night.onsetLabel, color = IndigoLight, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(night.wakeLabel, color = IndigoLight, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth().height(7.dp).background(SurfaceDark, RoundedCornerShape(50))) {
                Box(Modifier.fillMaxWidth(0.78f).fillMaxHeight().background(IndigoAccent, RoundedCornerShape(50)))
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(percent: Int, high: Boolean) {
    val badgeColor = if (high) SuccessGreen else WarningAmber
    Surface(shape = RoundedCornerShape(12.dp), color = badgeColor.copy(alpha = 0.12f)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${percent}%", color = badgeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("confidence", color = MutedGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun InsightRow(night: SleepMetricUiModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SmallMetric("MICRO-WAKES", night.microAwakenings.toString(), Modifier.weight(1f))
        SmallMetric("QUALITY", if (night.isHighConfidence) "High" else "Fair", Modifier.weight(1f))
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = CardSurface, modifier = modifier) {
        Column(Modifier.padding(15.dp)) {
            Text(label, color = MutedGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, color = SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SocialJetlagCard(hours: Float) {
    val severe = hours > 1.5f
    val statusColor = if (severe) WarningAmber else SuccessGreen
    Surface(shape = RoundedCornerShape(16.dp), color = CardSurface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SOCIAL JETLAG", color = MutedGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(String.format("%.1f hours", hours), color = SoftWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(if (severe) "Worth watching" else "Looks healthy", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, color = SoftWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = MutedGray, fontSize = 12.sp)
    }
}

@Composable
private fun TrendCard(trend: List<DayTrendUiModel>) {
    val maxHours = (trend.maxOfOrNull { it.durationHours } ?: 1f).coerceAtLeast(1f)
    Surface(shape = RoundedCornerShape(20.dp), color = CardSurface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            trend.forEach { day ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(day.dayLabel, color = MutedGray, fontSize = 11.sp, modifier = Modifier.width(34.dp))
                    Box(Modifier.weight(1f).height(10.dp).background(SurfaceDark, RoundedCornerShape(50))) {
                        Box(Modifier.fillMaxWidth((day.durationHours / maxHours).coerceIn(0.04f, 1f)).fillMaxHeight().background(IndigoAccent, RoundedCornerShape(50)))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(String.format("%.1fh", day.durationHours), color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ActionCard(onInference: () -> Unit, onRefresh: () -> Unit, loading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Button(onClick = onInference, enabled = !loading, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)) {
            Text("Analyze sleep now", color = SoftWhite, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoLight)) {
            Text("Refresh data", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = ErrorRose.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
        Text(message, color = ErrorRose, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(14.dp))
    }
}
