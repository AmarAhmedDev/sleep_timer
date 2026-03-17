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
        
        // STEP 1: IMMEDIATELY pause via MediaSession
        Log.d(TAG, "Step 1: MediaSession pause (immediate)")
        try {
            NotificationListener.pauseAllMedia(context)
        } catch (e: Exception) {
            Log.e(TAG, "Step 1 failed: ${e.message}")
        }
        
        // STEP 2: Request PERMANENT audio focus (100ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 2: Requesting PERMANENT audio focus")
                requestAudioFocusPermanent(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 2 failed: ${e.message}")
            }
        }, 100)
        
        // STEP 3: Send AudioManager key events (200ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 3: AudioManager key events")
                sendAudioManagerKeyEvents(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 3 failed: ${e.message}")
            }
        }, 200)
        
        // STEP 4: Second MediaSession pause + broadcast (400ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 4: Second MediaSession pause + broadcast")
                NotificationListener.pauseAllMedia(context)
                sendBroadcastPause(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 4 failed: ${e.message}")
            }
        }, 400)
        
        // STEP 5: Go to home screen (600ms)
        handler.postDelayed({
            Log.d(TAG, "Step 5: Going to home screen")
            goToHomeScreen(context)
        }, 600)
        
        // STEP 6: Third MediaSession pause after home (900ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 6: Third MediaSession pause (post-home)")
                NotificationListener.pauseAllMedia(context)
                sendAudioManagerKeyEvents(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 6 failed: ${e.message}")
            }
        }, 900)
        
        // STEP 7: Mute audio as safety net (1100ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 7: Muting audio as safety net")
                muteAudio(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 7 failed: ${e.message}")
            }
        }, 1100)
        
        // STEP 8: Fourth MediaSession pause + audio focus (1400ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 8: Fourth MediaSession pause")
                NotificationListener.pauseAllMedia(context)
                requestAudioFocusPermanent(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 8 failed: ${e.message}")
            }
        }, 1400)
        
        // STEP 9: Lock screen (1700ms)
        handler.postDelayed({
            Log.d(TAG, "Step 9: Locking screen")
            lockScreen(context)
        }, 1700)
        
        // STEP 10: Final pause after lock (2200ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 10: Final MediaSession pause after lock")
                NotificationListener.pauseAllMedia(context)
                sendAudioManagerKeyEvents(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 10 failed: ${e.message}")
            }
        }, 2200)
        
        // STEP 11: Last resort pause (3000ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 11: Last resort pause at 3s")
                NotificationListener.pauseAllMedia(context)
            } catch (e: Exception) {
                Log.e(TAG, "Step 11 failed: ${e.message}")
            }
        }, 3000)
    }
    
    private fun sendBroadcastPause(context: Context) {
        try {
            val pauseIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            context.sendBroadcast(pauseIntent)
            
            val musicPause = Intent("com.android.music.musicservicecommand")
            musicPause.putExtra("command", "pause")
            context.sendBroadcast(musicPause)
            
            // Samsung-specific broadcast  
            val samsungIntent = Intent("com.sec.android.app.music.musicservicecommand")
            samsungIntent.putExtra("command", "pause")
            context.sendBroadcast(samsungIntent)
            
            Log.d(TAG, "Broadcast pause sent (including Samsung)")
        } catch (e: Exception) {
            Log.e(TAG, "Broadcast failed: ${e.message}")
        }
    }
    
    private fun requestAudioFocusPermanent(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                
                // AUDIOFOCUS_GAIN forces all other apps to stop completely
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { }
                    .build()
                
                val result = audioManager.requestAudioFocus(focusRequest)
                Log.d(TAG, "Audio focus GAIN result: $result")
                
                // Abandon focus after 2s to clean up
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        audioManager.abandonAudioFocusRequest(focusRequest)
                    } catch (e: Exception) { }
                }, 2000)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio focus failed: ${e.message}")
        }
    }
    
    private fun sendShellMediaCommands() {
        try {
            executeShellCommand("input keyevent 127")
            executeShellCommand("input keyevent 85")
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
                .setSmallIcon(R.drawable.ic_tile_sleep_timer)
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
