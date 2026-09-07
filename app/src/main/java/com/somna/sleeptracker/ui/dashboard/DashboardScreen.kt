package com.somna.sleeptracker.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.somna.sleeptracker.data.service.TelemetryGuardianService
import com.somna.sleeptracker.ui.theme.*
import kotlinx.coroutines.delay

private enum class SomnaTab { Home, History, Insights, Settings }

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var tab by remember { mutableStateOf(SomnaTab.Home) }
    var tracking by remember { mutableStateOf(false) }

    Surface(color = DeepBlack, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    SomnaTab.Home -> HomeScreen(state, tracking, onStart = {
                        TelemetryGuardianService.start(it)
                        tracking = true
                    }, onAnalyze = { viewModel.onEvent(DashboardEvent.RunManualInference) }, onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) })
                    SomnaTab.History -> HistoryScreen(state)
                    SomnaTab.Insights -> InsightsScreen(state)
                    SomnaTab.Settings -> SettingsScreen()
                }
            }
            BottomBar(tab) { tab = it }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("☾", color = IndigoLight, fontSize = 30.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Somna", color = SoftWhite, fontSize = 21.sp, fontWeight = FontWeight.Medium)
                Text("Sleep better. Live brighter.", color = MutedGray, fontSize = 10.sp)
            }
        }
        Box(Modifier.size(38.dp).background(CardSurface, CircleShape), contentAlignment = Alignment.Center) {
            Text("M", color = IndigoLight, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeScreen(state: DashboardState, tracking: Boolean, onStart: (Context) -> Unit, onAnalyze: () -> Unit, onRefresh: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { AppHeader() }
        item {
            if (tracking) TrackingCard() else if (state.lastNight != null) HomeSummary(state.lastNight!!) else ReadyCard()
        }
        if (!tracking && state.lastNight == null) {
            item { Button(onClick = { onStart(context) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)) { Text("▶  Start Sleep Tracking", fontWeight = FontWeight.SemiBold) } }
        }
        if (state.lastNight != null && !tracking) {
            item { QuickInsight(state.lastNight!!) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onAnalyze, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoLight)) { Text("Analyze") }
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoLight)) { Text("Refresh") }
            }
        }
        state.errorMessage?.let { item { ErrorCard(it) } }
    }
}

@Composable
private fun ReadyCard() {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☾", color = IndigoLight, fontSize = 72.sp)
            Spacer(Modifier.height(6.dp))
            Text("Ready to sleep?", color = SoftWhite, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Tap the button below to start tracking your sleep. Keep your phone nearby, on your bed or nightstand.", color = MutedGray, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))
            Text("Tracking works in the background while you sleep.", color = IndigoLight, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TrackingCard() {
    var seconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { while (true) { delay(1000); seconds++ } }
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☾", color = IndigoLight, fontSize = 58.sp)
            Text("Tracking your sleep", color = SoftWhite, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text("Keep your phone nearby", color = MutedGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Text(String.format("%02d:%02d:%02d", h, m, s), color = SoftWhite, fontSize = 36.sp, fontWeight = FontWeight.Light)
            Text("Time sleeping", color = MutedGray, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            Surface(color = CardSurface, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) { Text("■  Tracking in background", color = SoftWhite, modifier = Modifier.padding(15.dp), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun HomeSummary(night: SleepMetricUiModel) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Last Night", color = SoftWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text("Sleep session", color = MutedGray, fontSize = 11.sp) }
                Surface(color = SuccessGreen.copy(alpha = .14f), shape = RoundedCornerShape(20.dp)) { Text("Good", color = SuccessGreen, modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp), fontSize = 12.sp) }
            }
            Spacer(Modifier.height(12.dp))
            Text(String.format("%.1f h", night.durationHours), color = SoftWhite, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("${night.onsetLabel} – ${night.wakeLabel}", color = MutedGray, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            SleepBars()
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("★ Sleep Quality", color = SoftWhite, fontSize = 13.sp); Text("${night.confidencePercent}%", color = SoftWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SleepBars() {
    Row(Modifier.fillMaxWidth().height(62.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        listOf(.45f,.75f,.55f,.9f,.68f,.82f,.52f,.72f,.6f,.8f,.5f,.7f).forEachIndexed { i, v -> Box(Modifier.weight(1f).fillMaxHeight(v).background(if (i % 3 == 0) IndigoLight else IndigoAccent, RoundedCornerShape(3.dp))) }
    }
}

@Composable
private fun QuickInsight(night: SleepMetricUiModel) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Text("☾", color = IndigoLight, fontSize = 28.sp); Spacer(Modifier.width(12.dp)); Column { Text("You fell asleep quickly", color = SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text("${night.microAwakenings} micro-wakes detected during the session.", color = MutedGray, fontSize = 11.sp) } }
    }
}

@Composable
private fun HistoryScreen(state: DashboardState) {
    LazyColumn(contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AppHeader() }
        item { Text("Sleep History", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.SemiBold) }
        if (state.weeklyTrend.isEmpty()) item { EmptyPanel("No nights recorded yet", "Start tracking tonight and your history will appear here.") }
        state.weeklyTrend.reversed().forEach { day -> item { HistoryRow(day) } }
    }
}

@Composable
private fun HistoryRow(day: DayTrendUiModel) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Text(day.dayLabel, color = MutedGray, modifier = Modifier.weight(1f)); Text(String.format("%.1fh", day.durationHours), color = SoftWhite, fontWeight = FontWeight.SemiBold); Spacer(Modifier.width(18.dp)); Text("★", color = WarningAmber) }
    }
}

@Composable
private fun InsightsScreen(state: DashboardState) {
    LazyColumn(contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { AppHeader() }
        item { Text("Weekly Insights", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.SemiBold) }
        item { TrendPanel(state.weeklyTrend) }
        item { InsightPanel("Sleep Quality", if (state.lastNight?.isHighConfidence == true) "High confidence" else "Build more nights", "More nights improve the reliability of your sleep estimates.") }
        state.socialJetlagHours?.let { item { InsightPanel("Social Jetlag", String.format("%.1f hours", it), if (it > 1.5f) "Worth watching" else "Looks healthy") } }
    }
}

@Composable
private fun TrendPanel(trend: List<DayTrendUiModel>) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text("Average Sleep", color = SoftWhite, fontSize = 13.sp)
            val avg = if (trend.isEmpty()) 0f else trend.map { it.durationHours }.average().toFloat()
            Text(if (avg == 0f) "—" else String.format("%.1fh", avg), color = SoftWhite, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            SleepBars()
            Spacer(Modifier.height(8.dp))
            Text("Your recent sleep duration", color = MutedGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InsightPanel(title: String, value: String, detail: String) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(17.dp)) { Text(title, color = MutedGray, fontSize = 11.sp); Text(value, color = SoftWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(detail, color = MutedGray, fontSize = 11.sp, lineHeight = 17.sp) } }
}

@Composable
private fun SettingsScreen() {
    var notifications by remember { mutableStateOf(true) }
    var goal by remember { mutableFloatStateOf(8f) }
    LazyColumn(contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { AppHeader() }
        item { Text("Settings", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.SemiBold) }
        item { SettingRow("◉", "Sleep Goal", String.format("%.0f hours", goal)) { goal = if (goal >= 9f) 6f else goal + 1f } }
        item { SettingToggle("♧", "Notifications", "Bedtime and sleep updates", notifications) { notifications = it } }
        item { SettingRow("◈", "Theme", "System / dark sleep theme") {} }
        item { SettingRow("▣", "Data & Privacy", "On-device only") {} }
        item { SettingRow("⇩", "Export Data", "Download your sleep data") {} }
        item { SettingRow("ⓘ", "About", "Somna v1.0.0") {} }
    }
}

@Composable
private fun SettingRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, color = IndigoLight, fontSize = 20.sp, modifier = Modifier.width(35.dp)); Column(Modifier.weight(1f)) { Text(title, color = SoftWhite, fontSize = 13.sp); Text(subtitle, color = MutedGray, fontSize = 10.sp) }; Text("›", color = MutedGray, fontSize = 22.sp) } }
}

@Composable
private fun SettingToggle(icon: String, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, color = IndigoLight, fontSize = 20.sp, modifier = Modifier.width(35.dp)); Column(Modifier.weight(1f)) { Text(title, color = SoftWhite, fontSize = 13.sp); Text(subtitle, color = MutedGray, fontSize = 10.sp) }; Switch(checked = checked, onCheckedChange = onCheckedChange) } }
}

@Composable
private fun EmptyPanel(title: String, subtitle: String) { Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(22.dp)) { Text(title, color = SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)); Text(subtitle, color = MutedGray, fontSize = 12.sp, lineHeight = 18.sp) } } }

@Composable
private fun ErrorCard(message: String) { Surface(color = ErrorRose.copy(alpha = .10f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) { Text(message, color = ErrorRose, fontSize = 12.sp, modifier = Modifier.padding(14.dp)) } }

@Composable
private fun BottomBar(selected: SomnaTab, onSelect: (SomnaTab) -> Unit) {
    Surface(color = SurfaceDark, tonalElevation = 3.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            BottomItem("⌂", "Home", selected == SomnaTab.Home) { onSelect(SomnaTab.Home) }
            BottomItem("◷", "History", selected == SomnaTab.History) { onSelect(SomnaTab.History) }
            BottomItem("▥", "Insights", selected == SomnaTab.Insights) { onSelect(SomnaTab.Insights) }
            BottomItem("⚙", "Settings", selected == SomnaTab.Settings) { onSelect(SomnaTab.Settings) }
        }
    }
}

@Composable
private fun BottomItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.width(76.dp).clickable(onClick = onClick).padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, color = if (selected) IndigoLight else MutedGray, fontSize = 20.sp)
        Text(label, color = if (selected) IndigoLight else MutedGray, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
