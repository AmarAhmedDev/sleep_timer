package com.example.sleep_timer

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SleepTimerTileService : TileService() {
    private val TAG = "SleepTimerTile"
    private lateinit var prefs: SharedPreferences
    private var countDownTimer: CountDownTimer? = null

    companion object {
        private const val PREFS_NAME = "sleep_timer_tile"
        private const val KEY_IS_TIMER_ACTIVE = "is_timer_active"
        private const val KEY_DEFAULT_DURATION_MINUTES = "default_duration_minutes"
        private const val KEY_REMAINING_TIME_MS = "remaining_time_ms"
        private const val KEY_START_TIME_MS = "start_time_ms"
        private const val DEFAULT_DURATION = 30 // 30 minutes default
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile clicked")

        val isNotificationListenerEnabled = isNotificationListenerEnabled()
        
        if (!isNotificationListenerEnabled) {
            // Open Notification Listener settings
            Log.d(TAG, "Notification Listener not enabled, opening settings")
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
            return
        }

        // Toggle timer
        val isActive = prefs.getBoolean(KEY_IS_TIMER_ACTIVE, false)
        
        if (isActive) {
            // Stop timer
            stopTimer()
        } else {
            // Start timer with default duration
            val durationMinutes = prefs.getInt(KEY_DEFAULT_DURATION_MINUTES, DEFAULT_DURATION)
            startTimer(durationMinutes)
        }
        
        updateTile()
    }

    private fun startTimer(durationMinutes: Int) {
        Log.d(TAG, "Starting timer for $durationMinutes minutes")
        
        val durationMs = durationMinutes * 60 * 1000L
        
        // Save state
        prefs.edit().apply {
            putBoolean(KEY_IS_TIMER_ACTIVE, true)
            putLong(KEY_REMAINING_TIME_MS, durationMs)
            putLong(KEY_START_TIME_MS, System.currentTimeMillis())
            apply()
        }
        
        // Start countdown timer
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                prefs.edit().putLong(KEY_REMAINING_TIME_MS, millisUntilFinished).apply()
                updateTile()
            }

            override fun onFinish() {
                // Timer expired - trigger media pause and screen lock
                Log.d(TAG, "Timer expired!")
                triggerSleepActions()
                stopTimer()
            }
        }.start()
        
        updateTile()
    }

    private fun stopTimer() {
        Log.d(TAG, "Stopping timer")
        
        countDownTimer?.cancel()
        countDownTimer = null
        
        prefs.edit().apply {
            putBoolean(KEY_IS_TIMER_ACTIVE, false)
            putLong(KEY_REMAINING_TIME_MS, 0)
            putLong(KEY_START_TIME_MS, 0)
            apply()
        }
        
        updateTile()
    }

    private fun triggerSleepActions() {
        // Trigger the media pause and screen lock functionality
        val intent = Intent("com.example.sleep_timer.TIMER_EXPIRED")
        sendBroadcast(intent)
        
        // Also call MainActivity methods directly if possible
        // This will mute, pause media, and lock screen
        try {
            val mainIntent = packageManager.getLaunchIntentForPackage(packageName)
            mainIntent?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("trigger_sleep_actions", true)
            }
            startActivity(mainIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering sleep actions: ${e.message}")
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        val isNotificationListenerEnabled = isNotificationListenerEnabled()
        val isTimerActive = prefs.getBoolean(KEY_IS_TIMER_ACTIVE, false)
        
        when {
            !isNotificationListenerEnabled -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Sleep Timer"
                tile.subtitle = "Enable permission"
            }
            isTimerActive -> {
                tile.state = Tile.STATE_ACTIVE
                val remainingMs = prefs.getLong(KEY_REMAINING_TIME_MS, 0)
                val remainingMinutes = (remainingMs / 60000).toInt()
                val remainingSeconds = ((remainingMs % 60000) / 1000).toInt()
                
                tile.label = "Sleep Timer"
                if (remainingMinutes > 0) {
                    tile.subtitle = "${remainingMinutes}m ${remainingSeconds}s"
                } else {
                    tile.subtitle = "${remainingSeconds}s"
                }
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                val defaultDuration = prefs.getInt(KEY_DEFAULT_DURATION_MINUTES, DEFAULT_DURATION)
                tile.label = "Sleep Timer"
                tile.subtitle = "Start ${defaultDuration}min"
            }
        }
        
        tile.updateTile()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            val packageName = packageName
            enabledListeners?.contains(packageName) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification listener: ${e.message}")
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

