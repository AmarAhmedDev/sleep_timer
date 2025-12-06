package com.example.sleep_timer

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SleepTimerTileService : TileService() {
    private val TAG = "SleepTimerTile"
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "sleep_timer_tile"
        private const val FLUTTER_PREFS_NAME = "FlutterSharedPreferences" // Flutter's default
        private const val KEY_IS_TIMER_ACTIVE = "is_timer_active"
        private const val KEY_DEFAULT_DURATION_MINUTES = "default_duration_minutes"
        private const val FLUTTER_KEY_PREFIX = "flutter."
        private const val KEY_REMAINING_TIME_MS = "remaining_time_ms"
        private const val KEY_START_TIME_MS = "start_time_ms"
        private const val DEFAULT_DURATION = 120 // 2 hours for testing
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
            // Start timer with duration from Flutter settings (user-set) or default
            val durationMinutes = getUserSetDuration()
            startTimer(durationMinutes)
        }
        
        updateTile()
    }
    
    private fun getUserSetDuration(): Int {
        Log.d(TAG, "=== getUserSetDuration() called ===")
        
        // Try to read from Flutter SharedPreferences
        try {
            val flutterPrefs = getSharedPreferences(FLUTTER_PREFS_NAME, MODE_PRIVATE)
            val flutterKey = FLUTTER_KEY_PREFIX + KEY_DEFAULT_DURATION_MINUTES
            
            Log.d(TAG, "Looking for key: $flutterKey")
            
            if (flutterPrefs.contains(flutterKey)) {
                val duration = flutterPrefs.getInt(flutterKey, DEFAULT_DURATION)
                Log.d(TAG, "✓ Found duration in Flutter prefs: $duration minutes")
                return duration
            } else {
                Log.w(TAG, "✗ Key '$flutterKey' not found in Flutter prefs")
                
                // Debug: log all keys
                val allKeys = flutterPrefs.all.keys
                Log.d(TAG, "Available keys in Flutter prefs: $allKeys")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Flutter prefs: ${e.message}")
            e.printStackTrace()
        }
        
        // Fallback to default
        Log.d(TAG, "Using default duration: $DEFAULT_DURATION minutes")
        return DEFAULT_DURATION
    }

    private fun startTimer(durationMinutes: Int) {
        Log.d(TAG, "Starting timer for $durationMinutes minutes")
        
        val durationMs = durationMinutes * 60 * 1000L
        val triggerAtMillis = System.currentTimeMillis() + durationMs
        
        // Save state (save total duration, not remaining)
        prefs.edit().apply {
            putBoolean(KEY_IS_TIMER_ACTIVE, true)
            putLong(KEY_REMAINING_TIME_MS, durationMs) // Total duration
            putLong(KEY_START_TIME_MS, System.currentTimeMillis())
            apply()
        }
        
        // Use AlarmManager for reliable background timer
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, TileTimerReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set exact alarm
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
        
        Log.d(TAG, "Timer scheduled via AlarmManager for ${durationMinutes}min")
        updateTile()
    }

    private fun stopTimer() {
        Log.d(TAG, "Stopping timer")
        
        // Cancel alarm
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, TileTimerReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        
        // Cancel countdown notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(1)
        
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
                
                // Calculate remaining time from start time
                val startTime = prefs.getLong(KEY_START_TIME_MS, 0)
                val totalDuration = prefs.getLong(KEY_REMAINING_TIME_MS, 0)
                val elapsed = System.currentTimeMillis() - startTime
                val remainingMs = maxOf(0, totalDuration - elapsed)
                
                val remainingMinutes = (remainingMs / 60000).toInt()
                val remainingSeconds = ((remainingMs % 60000) / 1000).toInt()
                
                tile.label = "Sleep Timer"
                if (remainingMinutes > 0) {
                    tile.subtitle = "${remainingMinutes}m ${remainingSeconds}s"
                } else {
                    tile.subtitle = "${remainingSeconds}s"
                }
                
                // Update countdown notification
                showCountdownNotification(remainingMs / 1000)
                
                // Schedule next update in 1 second if still active
                if (remainingMs > 0) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (prefs.getBoolean(KEY_IS_TIMER_ACTIVE, false)) {
                            updateTile()
                        }
                    }, 1000)
                }
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                val defaultDuration = getUserSetDuration()
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
    
    private fun showCountdownNotification(remainingSeconds: Long) {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create notification channel (Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "sleep_timer_ongoing_v2",
                    "Active Timer",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shows active sleep timer countdown"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            // Format time
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            
            val timeStr = if (hours > 0) {
                String.format("%dh %dm %ds", hours, minutes, seconds)
            } else if (minutes > 0) {
                String.format("%dm %ds", minutes, seconds)
            } else {
                String.format("%ds", seconds)
            }
            
            // Build notification
            val notification = android.app.Notification.Builder(this, "sleep_timer_ongoing_v2")
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    }
                }
                .setContentTitle("Sleep Timer Active")
                .setContentText("$timeStr remaining")
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true) // Alert only the first time
                .setDefaults(android.app.Notification.DEFAULT_ALL) // Sound and vibration
                .build()
            
            notificationManager.notify(1, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing countdown notification: ${e.message}")
        }
    }
}

