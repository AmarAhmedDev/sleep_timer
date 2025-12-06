package com.example.sleep_timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat

class TileTimerReceiver : BroadcastReceiver() {
    private val TAG = "TileTimerReceiver"
    private val NOTIFICATION_CHANNEL_ID = "sleep_timer_completion"
    private val COMPLETION_NOTIFICATION_ID = 999

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "========== TILE TIMER EXPIRED ==========")
        
        // Clear timer state
        val prefs = context.getSharedPreferences("sleep_timer_tile", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_timer_active", false)
            putLong("remaining_time_ms", 0)
            putLong("start_time_ms", 0)
            apply()
        }
        
        // Cancel ongoing countdown notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        
        // Execute sleep actions directly
        executeSleepActions(context)
        
        // Show completion notification
        showCompletionNotification(context)
        
        Log.d(TAG, "========== SLEEP ACTIONS COMPLETED ==========")
    }
    
    private fun executeSleepActions(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        
        // STEP 1: Pause media using NotificationListener
        Log.d(TAG, "Step 1: Pausing via NotificationListener...")
        try {
            NotificationListener.pauseAllMedia(context)
        } catch (e: Exception) {
            Log.e(TAG, "NotificationListener pause failed: ${e.message}")
        }
        
        // STEP 2: Send shell media commands
        Log.d(TAG, "Step 2: Sending shell media commands...")
        sendShellMediaCommands()
        
        // STEP 3: Send AudioManager key events
        Log.d(TAG, "Step 3: Sending AudioManager key events...")
        sendAudioManagerKeyEvents(context)
        
        // STEP 4: Mute audio
        Log.d(TAG, "Step 4: Muting audio...")
        muteAudio(context)
        
        // STEP 5: Go to home screen (after short delay)
        handler.postDelayed({
            Log.d(TAG, "Step 5: Going to home screen...")
            goToHomeScreen(context)
        }, 500)
        
        // STEP 6: Pause media again
        handler.postDelayed({
            Log.d(TAG, "Step 6: Second pause attempt...")
            try {
                NotificationListener.pauseAllMedia(context)
            } catch (e: Exception) {
                Log.e(TAG, "Second pause failed: ${e.message}")
            }
            sendShellMediaCommands()
        }, 800)
        
        // STEP 7: Lock screen
        handler.postDelayed({
            Log.d(TAG, "Step 7: Locking screen...")
            lockScreen(context)
        }, 1200)
        
        // STEP 8: Vibrate
        handler.postDelayed({
            Log.d(TAG, "Step 8: Vibrating...")
            vibrate(context)
        }, 600)
    }
    
    private fun sendShellMediaCommands() {
        try {
            // KEYCODE_MEDIA_PAUSE = 127
            executeShellCommand("input keyevent 127")
            // KEYCODE_MEDIA_PLAY_PAUSE = 85
            executeShellCommand("input keyevent 85")
            // KEYCODE_MEDIA_STOP = 86
            executeShellCommand("input keyevent 86")
            Log.d(TAG, "Shell media commands sent")
        } catch (e: Exception) {
            Log.e(TAG, "Shell commands failed: ${e.message}")
        }
    }
    
    private fun executeShellCommand(command: String) {
        try {
            Runtime.getRuntime().exec(command)
        } catch (e: Exception) {
            Log.e(TAG, "Command failed: $command")
        }
    }
    
    private fun sendAudioManagerKeyEvents(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Send PAUSE
            var event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            // Send PLAY_PAUSE
            event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            Log.d(TAG, "AudioManager key events sent")
        } catch (e: Exception) {
            Log.e(TAG, "AudioManager events failed: ${e.message}")
        }
    }
    
    private fun muteAudio(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    0
                )
            }
            Log.d(TAG, "Audio muted")
        } catch (e: Exception) {
            Log.e(TAG, "Mute failed: ${e.message}")
        }
    }
    
    private fun goToHomeScreen(context: Context) {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                               Intent.FLAG_ACTIVITY_CLEAR_TOP or
                               Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(homeIntent)
            Log.d(TAG, "Home screen intent sent")
        } catch (e: Exception) {
            Log.e(TAG, "Go to home failed: ${e.message}")
        }
    }
    
    private fun lockScreen(context: Context) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, SleepTimerDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                devicePolicyManager.lockNow()
                Log.d(TAG, "Screen locked successfully")
            } else {
                Log.w(TAG, "Device Admin not active, cannot lock screen")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lock screen failed: ${e.message}")
        }
    }
    
    private fun vibrate(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 200, 100, 400, 100, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 200, 100, 400, 100, 200), -1)
            }
            Log.d(TAG, "Vibration triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Vibrate failed: ${e.message}")
        }
    }
    
    private fun showCompletionNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel (Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Sleep Timer Completion",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications when sleep timer completes"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            // Build notification
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Sleep Timer Complete")
                .setContentText("Timer expired. Media paused and screen locked.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
            Log.d(TAG, "Completion notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Show notification failed: ${e.message}")
        }
    }
}
