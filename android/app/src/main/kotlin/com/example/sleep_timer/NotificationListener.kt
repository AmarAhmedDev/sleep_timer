package com.example.sleep_timer

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.Looper
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private val TAG = "SleepTimerNLS"

    companion object {
        private var instance: NotificationListener? = null
        
        fun pauseAllMedia(context: Context) {
            Log.d("SleepTimerNLS", "=== pauseAllMedia called ===")
            
            if (instance != null) {
                Log.d("SleepTimerNLS", "Using instance method to pause...")
                instance?.pauseMediaSessions()
            } else {
                Log.w("SleepTimerNLS", "Instance is NULL, trying MediaSessionManager directly...")
                // Fallback: try using MediaSessionManager directly
                tryPauseViaMediaSessionManager(context)
            }
        }
        
        private fun tryPauseViaMediaSessionManager(context: Context) {
            try {
                val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                val componentName = ComponentName(context, NotificationListener::class.java)
                
                val activeSessions = try {
                    mediaSessionManager.getActiveSessions(componentName)
                } catch (e: SecurityException) {
                    Log.e("SleepTimerNLS", "SecurityException - Notification Listener permission not granted")
                    emptyList<MediaController>()
                }
                
                Log.d("SleepTimerNLS", "Found ${activeSessions.size} sessions via fallback")
                
                for (controller in activeSessions) {
                    pauseController(controller, "SleepTimerNLS")
                }
            } catch (e: Exception) {
                Log.e("SleepTimerNLS", "Fallback pause failed: ${e.message}")
            }
        }
        
        private fun pauseController(controller: MediaController, tag: String) {
            try {
                val packageName = controller.packageName
                val playbackState = controller.playbackState
                
                Log.d(tag, "Session: $packageName")
                
                if (playbackState != null) {
                    val stateInt = playbackState.state
                    val stateName = when (stateInt) {
                        PlaybackState.STATE_PLAYING -> "PLAYING"
                        PlaybackState.STATE_PAUSED -> "PAUSED"
                        PlaybackState.STATE_STOPPED -> "STOPPED"
                        PlaybackState.STATE_BUFFERING -> "BUFFERING"
                        PlaybackState.STATE_CONNECTING -> "CONNECTING"
                        PlaybackState.STATE_FAST_FORWARDING -> "FAST_FORWARDING"
                        PlaybackState.STATE_REWINDING -> "REWINDING"
                        PlaybackState.STATE_SKIPPING_TO_NEXT -> "SKIPPING_TO_NEXT"
                        PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> "SKIPPING_TO_PREVIOUS"
                        else -> "UNKNOWN($stateInt)"
                    }
                    Log.d(tag, "  State: $stateName")
                    
                    // Pause if playing, buffering, or any active state
                    val isActive = stateInt == PlaybackState.STATE_PLAYING ||
                                   stateInt == PlaybackState.STATE_BUFFERING ||
                                   stateInt == PlaybackState.STATE_CONNECTING ||
                                   stateInt == PlaybackState.STATE_FAST_FORWARDING ||
                                   stateInt == PlaybackState.STATE_REWINDING ||
                                   stateInt == PlaybackState.STATE_SKIPPING_TO_NEXT ||
                                   stateInt == PlaybackState.STATE_SKIPPING_TO_PREVIOUS
                    
                    if (isActive) {
                        // Send PAUSE command - this is the primary and most reliable command
                        Log.d(tag, "  >>> Sending PAUSE to $packageName")
                        controller.transportControls.pause()
                        Log.d(tag, "  ✓ PAUSE sent to $packageName")
                    } else {
                        Log.d(tag, "  → Not active ($stateName), skipping")
                    }
                } else {
                    // If playback state is null, try to pause anyway
                    // Some players don't report state properly
                    Log.w(tag, "  PlaybackState is null for $packageName, sending pause anyway")
                    controller.transportControls.pause()
                }
            } catch (e: Exception) {
                Log.e(tag, "  Error pausing ${controller.packageName}: ${e.message}")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "=== NotificationListener CONNECTED ===")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "=== NotificationListener DISCONNECTED ===")
    }

    fun pauseMediaSessions() {
        try {
            Log.d(TAG, "--- Starting pauseMediaSessions() ---")
            
            // IMPORTANT: Use ComponentName instead of null for Samsung compatibility
            val componentName = ComponentName(this, NotificationListener::class.java)
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            
            val activeSessions = try {
                mediaSessionManager.getActiveSessions(componentName)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException with componentName, trying null...")
                try {
                    mediaSessionManager.getActiveSessions(null)
                } catch (e2: SecurityException) {
                    Log.e(TAG, "SecurityException with null too: ${e2.message}")
                    emptyList<MediaController>()
                }
            }
            
            Log.d(TAG, "Found ${activeSessions.size} active media sessions")
            
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "No active media sessions found!")
                return
            }
            
            // First pass: pause all active sessions
            for ((index, controller) in activeSessions.withIndex()) {
                Log.d(TAG, "--- Session $index ---")
                pauseController(controller, TAG)
            }
            
            // Second pass after 300ms: verify and re-pause if needed
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "--- Verification pass (300ms later) ---")
                try {
                    val sessionsAfter = try {
                        mediaSessionManager.getActiveSessions(componentName)
                    } catch (e: SecurityException) {
                        try { mediaSessionManager.getActiveSessions(null) }
                        catch (e2: Exception) { emptyList<MediaController>() }
                    }
                    
                    for (controller in sessionsAfter) {
                        val state = controller.playbackState?.state
                        if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING) {
                            Log.w(TAG, "${controller.packageName} is STILL playing! Re-pausing...")
                            controller.transportControls.pause()
                            
                            // Also try stop for stubborn players
                            Handler(Looper.getMainLooper()).postDelayed({
                                try {
                                    val checkState = controller.playbackState?.state
                                    if (checkState == PlaybackState.STATE_PLAYING) {
                                        Log.w(TAG, "${controller.packageName} STILL playing after re-pause, sending STOP")
                                        controller.transportControls.stop()
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in stop fallback: ${e.message}")
                                }
                            }, 500)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Verification pass failed: ${e.message}")
                }
            }, 300)
            
            Log.d(TAG, "--- pauseMediaSessions() initial pass completed ---")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Track media notifications
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not used
    }
}
