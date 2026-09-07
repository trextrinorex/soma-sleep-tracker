package com.somna.sleeptracker.ui.navigation

import androidx.compose.runtime.Composable
import com.somna.sleeptracker.ui.dashboard.DashboardScreen

/**
 * Simple single-destination host for v1.
 * Future phases can expand to History / Insights / Settings.
 */
@Composable
fun SomnaNavHost() {
    DashboardScreen()
}
