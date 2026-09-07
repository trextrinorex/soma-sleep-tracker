package com.somna.sleeptracker.data.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.somna.sleeptracker.R
import com.somna.sleeptracker.data.receiver.ScreenStateReceiver
import com.somna.sleeptracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sticky foreground service that keeps critical receivers alive and provides
 * a low-priority persistent notification required by Android for background work.
 * Designed for minimal battery impact (<1.2% target).
 */
@AndroidEntryPoint
class TelemetryGuardianService : Service() {

    private var screenReceiver: ScreenStateReceiver? = null

    override fun onCreate() {
        super.onCreate()
        registerScreenObservers()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterScreenObservers()
        super.onDestroy()
    }

    private fun registerScreenObservers() {
        screenReceiver = ScreenStateReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun unregisterScreenObservers() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        screenReceiver = null
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setContentTitle(getString(R.string.guardian_notification_title))
            .setContentText(getString(R.string.guardian_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, TelemetryGuardianService::class.java)
            context.startForegroundService(intent)
        }
    }
}
