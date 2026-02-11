package com.sleeptimer.app

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.KeyEvent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.DataOutputStream

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.sleeptimer/media_control"
    private val TAG = "SleepTimer"
    private var methodChannel: MethodChannel? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "stopMediaAndCloseApps" -> {
                    Log.d(TAG, "=== STOP MEDIA CALLED ===")
                    stopMediaAndCloseApps()
                    result.success(true)
                }
                "lockScreen" -> {
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
                    vibrate()
                    result.success(true)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun stopMediaAndCloseApps() {
        Log.d(TAG, "Starting media stop sequence...")
        
        val handler = Handler(Looper.getMainLooper())
        
        // Execute on main thread
        handler.post {
            try {
                // STEP 1: Use MediaSessionManager to PAUSE all active media sessions (MOST RELIABLE FOR AUDIO)
                Log.d(TAG, "Step 1: Pausing all media sessions via MediaSessionManager...")
                pauseAllMediaSessions()
                
                // STEP 2: Send shell commands for media keys
                Log.d(TAG, "Step 2: Sending shell media key commands...")
                sendShellMediaCommands()
                
                // STEP 3: Use AudioManager key events
                Log.d(TAG, "Step 3: Sending AudioManager key events...")
                sendAudioManagerKeyEvents()
                
                // STEP 4: Request audio focus
                Log.d(TAG, "Step 4: Requesting audio focus...")
                requestAudioFocusInterrupt()
                
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
        
        // STEP 6: Pause media sessions again + send pause commands
        handler.postDelayed({
            Log.d(TAG, "Step 6: Second pause attempt with MediaSessionManager...")
            pauseAllMediaSessions()
            sendShellMediaCommands()
            sendAudioManagerKeyEvents()
        }, 600)
        
        // STEP 7: Kill media app processes
        handler.postDelayed({
            Log.d(TAG, "Step 7: Killing media apps...")
            killMediaAppProcesses()
        }, 900)
        
        // STEP 8: Final pause attempt (no muting - we want to pause, not mute)
        handler.postDelayed({
            Log.d(TAG, "Step 8: Final pause attempt...")
            pauseAllMediaSessions()
            sendShellMediaCommands()
        }, 1200)
        
        // Vibrate to indicate timer complete
        handler.postDelayed({
            Log.d(TAG, "Vibrating to indicate completion...")
            vibrate()
        }, 500)
    }
    
    private fun pauseAllMediaSessions() {
        try {
            Log.d(TAG, "--- Attempting to pause all media sessions ---")
            
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            if (mediaSessionManager == null) {
                Log.e(TAG, "MediaSessionManager is null!")
                return
            }
            
            // Get the NotificationListener component for this app
            val listenerComponent = ComponentName(this, com.sleeptimer.app.NotificationListener::class.java)
            
            val activeSessions: List<MediaController>
            try {
                activeSessions = mediaSessionManager.getActiveSessions(listenerComponent)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException getting sessions - Notification Listener permission may not be granted")
                Log.e(TAG, "Falling back to other pause methods...")
                return
            }
            
            Log.d(TAG, "Found ${activeSessions.size} active media session(s)")
            
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "No active media sessions found!")
                return
            }
            
            for ((index, controller) in activeSessions.withIndex()) {
                try {
                    val packageName = controller.packageName
                    val playbackState = controller.playbackState
                    
                    Log.d(TAG, "Session $index: $packageName")
                    
                    if (playbackState != null) {
                        val stateName = when (playbackState.state) {
                            PlaybackState.STATE_PLAYING -> "PLAYING"
                            PlaybackState.STATE_PAUSED -> "PAUSED"
                            PlaybackState.STATE_STOPPED -> "STOPPED"
                            PlaybackState.STATE_BUFFERING -> "BUFFERING"
                            else -> "OTHER (${playbackState.state})"
                        }
                        Log.d(TAG, "  Current state: $stateName")
                        
                        // Send pause and stop commands regardless of state
                        Log.d(TAG, "  >>> Sending PAUSE command to $packageName")
                        controller.transportControls.pause()
                        
                        // Also send stop for good measure
                        Log.d(TAG, "  >>> Sending STOP command to $packageName")
                        controller.transportControls.stop()
                        
                        Log.d(TAG, "  ✓ Commands sent successfully to $packageName")
                    } else {
                        // Even without playback state, try to send pause
                        Log.w(TAG, "  PlaybackState is null, still attempting pause...")
                        controller.transportControls.pause()
                        controller.transportControls.stop()
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "  Error controlling session: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            Log.d(TAG, "--- pauseAllMediaSessions() completed ---")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in pauseAllMediaSessions: ${e.message}")
            Log.e(TAG, "Notification Listener permission may not be granted!")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in pauseAllMediaSessions: ${e.message}")
            e.printStackTrace()
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
                "com.sec.android.app.music" // Samsung Music
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
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Vibrate pattern: short-long-short
                val pattern = longArrayOf(0, 200, 100, 400, 100, 200)
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 200, 100, 400, 100, 200), -1)
            }
            Log.d(TAG, "Vibration triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating: ${e.message}")
        }
    }
}
