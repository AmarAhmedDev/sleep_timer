package com.example.sleep_timer

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.sleeptimer/media_control"
    private val TAG = "SleepTimer"
    private var methodChannel: MethodChannel? = null
    private var devicePolicyManager: DevicePolicyManager? = null
    private var adminComponent: ComponentName? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        Log.d(TAG, "=== MainActivity configureFlutterEngine CALLED ===")
        
        // Initialize Device Admin
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, SleepTimerDeviceAdminReceiver::class.java)
        
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel?.setMethodCallHandler { call, result ->
            Log.d(TAG, "Method called: ${call.method}")
            when (call.method) {
                "stopMediaAndCloseApps" -> {
                    Log.d(TAG, "=== STOP MEDIA CALLED ===")
                    stopMediaAndCloseApps()
                    result.success(true)
                }
                "lockScreen" -> {
                    Log.d(TAG, "lockScreen called")
                    val success = lockScreen()
                    result.success(success)
                }
                "requestDeviceAdmin" -> {
                    Log.d(TAG, "requestDeviceAdmin called")
                    requestDeviceAdminPermission()
                    result.success(true)
                }
                "isDeviceAdminActive" -> {
                    val isActive = isDeviceAdminActive()
                    result.success(isActive)
                }
                "checkNotificationListener" -> {
                    val isEnabled = isNotificationListenerEnabled()
                    result.success(isEnabled)
                }
                "requestNotificationListener" -> {
                    requestNotificationListenerPermission()
                    result.success(true)
                }
                "getForegroundApp" -> {
                    result.success(null)
                }
                "isMediaPlaying" -> {
                    val isPlaying = isMediaPlaying()
                    result.success(isPlaying)
                }
                "vibrate" -> {
                    Log.d(TAG, "vibrate called")
                    vibrate()
                    result.success(true)
                }
                "getTileDuration" -> {
                    // Get tile duration from SharedPreferences
                    val prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
                    val duration = prefs.getInt("flutter.default_duration_minutes", 30)
                    Log.d(TAG, "getTileDuration returning: $duration")
                    result.success(duration)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
        Log.d(TAG, "=== MethodChannel handler registered ===")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Check if triggered by tile timer expiration
        if (intent.getBooleanExtra("trigger_sleep_actions", false)) {
            Log.d(TAG, "=== TRIGGERED BY TILE TIMER EXPIRATION ===")
            stopMediaAndCloseApps()
        }
    }

    private fun stopMediaAndCloseApps() {
        Log.d(TAG, "=== Starting AGGRESSIVE media stop sequence ===")
        
        val handler = Handler(Looper.getMainLooper())
        
        // STEP 1: IMMEDIATELY pause via MediaSession (most reliable on Samsung)
        handler.post {
            try {
                Log.d(TAG, "Step 1: MediaSession pause (immediate)")
                NotificationListener.pauseAllMedia(this)
            } catch (e: Exception) {
                Log.e(TAG, "Step 1 failed: ${e.message}")
            }
        }
        
        // STEP 2: Request PERMANENT audio focus to force all apps to stop (100ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 2: Requesting PERMANENT audio focus")
                requestAudioFocusPermanent()
            } catch (e: Exception) {
                Log.e(TAG, "Step 2 failed: ${e.message}")
            }
        }, 100)
        
        // STEP 3: Send AudioManager key events (200ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 3: AudioManager key events")
                sendAudioManagerKeyEvents()
            } catch (e: Exception) {
                Log.e(TAG, "Step 3 failed: ${e.message}")
            }
        }, 200)
        
        // STEP 4: Second MediaSession pause + broadcast (400ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 4: Second MediaSession pause + broadcast")
                NotificationListener.pauseAllMedia(this)
                sendBroadcastPause()
            } catch (e: Exception) {
                Log.e(TAG, "Step 4 failed: ${e.message}")
            }
        }, 400)
        
        // STEP 5: Go to home screen (600ms)
        handler.postDelayed({
            Log.d(TAG, "Step 5: Going to home screen")
            goToHomeScreen()
        }, 600)
        
        // STEP 6: Third MediaSession pause after going home (900ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 6: Third MediaSession pause (post-home)")
                NotificationListener.pauseAllMedia(this)
                sendAudioManagerKeyEvents()
            } catch (e: Exception) {
                Log.e(TAG, "Step 6 failed: ${e.message}")
            }
        }, 900)
        
        // STEP 7: Mute audio as safety net (1100ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 7: Muting audio as safety net")
                muteAllAudio()
            } catch (e: Exception) {
                Log.e(TAG, "Step 7 failed: ${e.message}")
            }
        }, 1100)
        
        // STEP 8: Kill media app processes (1300ms)
        handler.postDelayed({
            Log.d(TAG, "Step 8: Killing media apps")
            killMediaAppProcesses()
        }, 1300)
        
        // STEP 9: Fourth MediaSession pause + audio focus (1500ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 9: Fourth MediaSession pause")
                NotificationListener.pauseAllMedia(this)
                requestAudioFocusPermanent()
            } catch (e: Exception) {
                Log.e(TAG, "Step 9 failed: ${e.message}")
            }
        }, 1500)
        
        // STEP 10: Lock screen (1800ms)
        handler.postDelayed({
            Log.d(TAG, "Step 10: Locking screen")
            lockScreen()
        }, 1800)
        
        // STEP 11: Final aggressive pause after lock (2200ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 11: Final MediaSession pause after lock")
                NotificationListener.pauseAllMedia(this)
                sendAudioManagerKeyEvents()
            } catch (e: Exception) {
                Log.e(TAG, "Step 11 failed: ${e.message}")
            }
        }, 2200)
        
        // STEP 12: Last resort pause (3000ms)
        handler.postDelayed({
            try {
                Log.d(TAG, "Step 12: Last resort pause at 3s")
                NotificationListener.pauseAllMedia(this)
            } catch (e: Exception) {
                Log.e(TAG, "Step 12 failed: ${e.message}")
            }
        }, 3000)
    }
    
    private fun sendBroadcastPause() {
        try {
            val pauseIntent = Intent("com.android.music.musicservicecommand")
            pauseIntent.putExtra("command", "pause")
            sendBroadcast(pauseIntent)
            
            val toggleIntent = Intent("com.android.music.musicservicecommand")
            toggleIntent.putExtra("command", "togglepause")
            sendBroadcast(toggleIntent)
            
            // Samsung-specific broadcast
            val samsungIntent = Intent("com.sec.android.app.music.musicservicecommand")
            samsungIntent.putExtra("command", "pause")
            sendBroadcast(samsungIntent)
            
            Log.d(TAG, "Broadcast pause commands sent (including Samsung)")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending broadcast: ${e.message}")
        }
    }
    
    private fun sendAudioManagerKeyEvents() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Send PAUSE key event (most important)
            var event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            // Send PLAY_PAUSE toggle  
            event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            Log.d(TAG, "AudioManager key events sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending key events: ${e.message}")
        }
    }
    
    // Use PERMANENT audio focus gain to force all other apps to stop
    private fun requestAudioFocusPermanent() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                    
                // Use AUDIOFOCUS_GAIN (permanent) instead of TRANSIENT
                // This forces other apps to give up audio focus completely
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { }
                    .build()
                    
                val result = audioManager.requestAudioFocus(focusRequest)
                Log.d(TAG, "Audio focus GAIN result: $result")
                
                // Abandon focus after a short delay to clean up
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
                Log.d(TAG, "Audio focus GAIN requested (legacy)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting audio focus: ${e.message}")
        }
    }
    
    private fun muteAllAudio() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Set volume to 0 for music stream
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            
            // Mute the stream
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    0
                )
            }
            
            Log.d(TAG, "Audio muted and volume set to 0")
        } catch (e: Exception) {
            Log.e(TAG, "Error muting audio: ${e.message}")
        }
    }
    
    private fun goToHomeScreen() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                               Intent.FLAG_ACTIVITY_CLEAR_TOP or
                               Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(homeIntent)
            Log.d(TAG, "Home intent sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error going to home: ${e.message}")
        }
    }
    
    private fun killMediaAppProcesses() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            val mediaApps = listOf(
                "com.google.android.youtube",
                "com.google.android.apps.youtube.music",
                "com.spotify.music",
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.facebook.katana",
                "com.instagram.android",
                "org.telegram.messenger",
                "com.mxtech.videoplayer.ad",
                "com.mxtech.videoplayer.pro",
                "com.android.music",
                "com.netflix.mediaclient",
                "com.amazon.avod.thirdpartyclient",
                "tv.twitch.android.app",
                "com.soundcloud.android",
                "com.apple.android.music",
                "com.sec.android.app.music"
            )
            
            for (packageName in mediaApps) {
                try {
                    activityManager.killBackgroundProcesses(packageName)
                } catch (e: Exception) { }
            }
            Log.d(TAG, "Media apps killed")
        } catch (e: Exception) {
            Log.e(TAG, "Error killing apps: ${e.message}")
        }
    }

    private fun lockScreen(): Boolean {
        return try {
            // Go to home first
            goToHomeScreen()
            
            // Then lock the screen if Device Admin is active
            if (isDeviceAdminActive()) {
                Log.d(TAG, "Device Admin is active, locking screen...")
                devicePolicyManager?.lockNow()
                Log.d(TAG, "Screen locked successfully")
                true
            } else {
                Log.w(TAG, "Device Admin not active, cannot lock screen")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error locking screen: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    private fun requestDeviceAdminPermission() {
        try {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Sleep Timer needs Device Admin permission to turn off the screen when the timer expires."
            )
            startActivityForResult(intent, 1001)
            Log.d(TAG, "Device Admin permission request launched")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Device Admin: ${e.message}")
        }
    }
    
    private fun isDeviceAdminActive(): Boolean {
        return try {
            devicePolicyManager?.isAdminActive(adminComponent!!) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Device Admin: ${e.message}")
            false
        }
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
    
    private fun requestNotificationListenerPermission() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Log.d(TAG, "Notification listener settings opened")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notification listener settings: ${e.message}")
        }
    }

    private fun isMediaPlaying(): Boolean {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.isMusicActive
        } catch (e: Exception) {
            Log.e(TAG, "Error checking media: ${e.message}")
        }
        return false
    }

    private fun vibrate() {
        try {
            Log.d(TAG, "Vibrating device...")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Vibrate pattern: short-long-short
                val pattern = longArrayOf(0, 200, 100, 400, 100, 200)
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 200, 100, 400, 100, 200), -1)
            }
            Log.d(TAG, "Vibration triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating: ${e.message}")
            e.printStackTrace()
        }
    }
}
