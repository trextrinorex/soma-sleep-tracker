package com.somna.sleeptracker.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.repository.SleepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: SleepRepository

    override fun onReceive(context: Context, intent: Intent) {
        val type = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                // Differentiate AC vs wireless if possible
                val status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                when (status) {
                    BatteryManager.BATTERY_PLUGGED_AC -> SystemEventType.POWER_CONNECTED_AC
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> SystemEventType.POWER_CONNECTED_WIRELESS
                    else -> SystemEventType.POWER_CONNECTED_AC
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> SystemEventType.POWER_DISCONNECTED
            else -> return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.logEvent(type)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
