package com.somna.sleeptracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.somna.sleeptracker.data.service.TelemetryGuardianService
import com.somna.sleeptracker.data.worker.WorkScheduler
import com.somna.sleeptracker.ui.dashboard.DashboardScreen
import com.somna.sleeptracker.ui.theme.DeepBlack
import com.somna.sleeptracker.ui.theme.SomnaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure background pipeline is alive
        TelemetryGuardianService.start(this)
        workScheduler.scheduleAll()

        setContent {
            SomnaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepBlack
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}
