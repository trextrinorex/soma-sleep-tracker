package com.somna.sleeptracker.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.somna.sleeptracker.data.local.entity.SystemEventType
import com.somna.sleeptracker.domain.repository.SleepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: SleepRepository

    override fun onReceive(context: Context, intent: Intent) {
        val type = when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> SystemEventType.SCREEN_OFF
            Intent.ACTION_SCREEN_ON -> SystemEventType.SCREEN_ON
            Intent.ACTION_USER_PRESENT -> SystemEventType.USER_PRESENT
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
