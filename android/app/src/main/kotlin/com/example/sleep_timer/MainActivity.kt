package com.example.sleep_timer

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
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

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        Log.d(TAG, "=== MainActivity configureFlutterEngine CALLED ===")
        
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
                    lockScreen()
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
        Log.d(TAG, "Starting media stop sequence...")
        
        val handler = Handler(Looper.getMainLooper())
        
        // Execute on main thread
        handler.post {
            try {
                // STEP 1: Send shell commands for media keys (MOST RELIABLE)
                Log.d(TAG, "Step 1: Sending shell media key commands...")
                sendShellMediaCommands()
                
                // STEP 2: Use AudioManager key events
                Log.d(TAG, "Step 2: Sending AudioManager key events...")
                sendAudioManagerKeyEvents()
                
                // STEP 3: Request audio focus
                Log.d(TAG, "Step 3: Requesting audio focus...")
                requestAudioFocusInterrupt()
                
                // STEP 4: Mute all audio
                Log.d(TAG, "Step 4: Muting audio...")
                muteAllAudio()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in initial stop: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // STEP 5: Go to home screen after short delay
        handler.postDelayed({
            Log.d(TAG, "Step 5: Going to home screen...")
            goToHomeScreen()
        }, 300)
        
        // STEP 6: Send pause again
        handler.postDelayed({
            Log.d(TAG, "Step 6: Second pause attempt...")
            sendShellMediaCommands()
            sendAudioManagerKeyEvents()
        }, 600)
        
        // STEP 7: Kill media app processes
        handler.postDelayed({
            Log.d(TAG, "Step 7: Killing media apps...")
            killMediaAppProcesses()
        }, 900)
        
        // STEP 8: Final mute and pause
        handler.postDelayed({
            Log.d(TAG, "Step 8: Final mute and pause...")
            muteAllAudio()
            sendShellMediaCommands()
        }, 1200)
        
        // Vibrate to indicate timer complete
        handler.postDelayed({
            Log.d(TAG, "Vibrating to indicate completion...")
            vibrate()
        }, 500)
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

    private fun lockScreen() {
        goToHomeScreen()
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
