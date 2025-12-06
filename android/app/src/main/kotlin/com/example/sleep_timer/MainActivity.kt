package com.example.sleep_timer

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
                else -> {
                    result.notImplemented()
                }
            }
        }
        Log.d(TAG, "=== MethodChannel handler registered ===")
    }

    private fun stopMediaAndCloseApps() {
        Log.d(TAG, "Starting enhanced media stop sequence...")
        
        val handler = Handler(Looper.getMainLooper())
        
        // Execute on main thread
        handler.post {
            try {
                // STEP 1: Use NotificationListener MediaSession control (MOST RELIABLE)
                Log.d(TAG, "Step 1: Pausing via MediaSession...")
                NotificationListener.pauseAllMedia(this)
                
                // STEP 2: Send shell commands for media keys
                Log.d(TAG, "Step 2: Sending shell media key commands...")
                sendShellMediaCommands()
                
                // STEP 3: Send broadcast pause commands (for apps like YouTube)
                Log.d(TAG, "Step 3: Sending broadcast pause...")
                sendBroadcastPause()
                
                // STEP 4: Use AudioManager key events
                Log.d(TAG, "Step 4: Sending AudioManager key events...")
                sendAudioManagerKeyEvents()
                
                // STEP 5: Request audio focus
                Log.d(TAG, "Step 5: Requesting audio focus...")
                requestAudioFocusInterrupt()
                
                // STEP 6: Mute all audio
                Log.d(TAG, "Step 6: Muting audio...")
                muteAllAudio()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in initial stop: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // STEP 7: Go to home screen after short delay
        handler.postDelayed({
            Log.d(TAG, "Step 7: Going to home screen...")
            goToHomeScreen()
        }, 400)
        
        // STEP 8: Send pause commands again
        handler.postDelayed({
            Log.d(TAG, "Step 8: Second pause attempt...")
            NotificationListener.pauseAllMedia(this)
            sendShellMediaCommands()
            sendBroadcastPause()
        }, 700)
        
        // STEP 9: Kill media app processes
        handler.postDelayed({
            Log.d(TAG, "Step 9: Killing media apps...")
            killMediaAppProcesses()
        }, 1000)
        
        // STEP 10: Lock screen if Device Admin is active
        handler.postDelayed({
            Log.d(TAG, "Step 10: Locking screen...")
            lockScreen()
        }, 1300)
        
        // STEP 11: Final pause attempt
        handler.postDelayed({
            Log.d(TAG, "Step 11: Final pause...")
            NotificationListener.pauseAllMedia(this)
            sendShellMediaCommands()
        }, 1500)
        
        // Vibrate to indicate timer complete
        handler.postDelayed({
            Log.d(TAG, "Vibrating to indicate completion...")
            vibrate()
        }, 500)
    }
    
    private fun sendBroadcastPause() {
        try {
            // Send broadcast intents to pause media
            val pauseIntent = Intent("com.android.music.musicservicecommand")
            pauseIntent.putExtra("command", "pause")
            sendBroadcast(pauseIntent)
            
            // Alternative broadcast for some players
            val toggleIntent = Intent("com.android.music.musicservicecommand")
            toggleIntent.putExtra("command", "togglepause")
            sendBroadcast(toggleIntent)
            
            Log.d(TAG, "Broadcast pause commands sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending broadcast: ${e.message}")
        }
    }
    
    private fun sendShellMediaCommands() {
        try {
            // Use shell command to send media key events
            // These are the most reliable way to pause media on Android
            
            // KEYCODE_MEDIA_PAUSE = 127
            executeShellCommand("input keyevent 127")
            
            // KEYCODE_MEDIA_PLAY_PAUSE = 85 
            executeShellCommand("input keyevent 85")
            
            // KEYCODE_MEDIA_STOP = 86
            executeShellCommand("input keyevent 86")
            
            Log.d(TAG, "Shell media commands sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending shell commands: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun executeShellCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            Log.d(TAG, "Executed: $command")
        } catch (e: Exception) {
            Log.e(TAG, "Shell command failed: $command - ${e.message}")
        }
    }
    
    private fun sendAudioManagerKeyEvents() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Send PAUSE
            var event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            // Send PLAY_PAUSE (toggle)
            event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(event)
            
            // Send STOP
            event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP)
            audioManager.dispatchMediaKeyEvent(event)
            event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP)
            audioManager.dispatchMediaKeyEvent(event)
            
            Log.d(TAG, "AudioManager key events sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending key events: ${e.message}")
        }
    }
    
    private fun requestAudioFocusInterrupt() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                    
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener { }
                    .build()
                    
                audioManager.requestAudioFocus(focusRequest)
                Log.d(TAG, "Audio focus requested (API 26+)")
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                Log.d(TAG, "Audio focus requested (legacy)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting audio focus: ${e.message}")
        }
    }
    
    private fun muteAllAudio() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Set volume to 0
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
