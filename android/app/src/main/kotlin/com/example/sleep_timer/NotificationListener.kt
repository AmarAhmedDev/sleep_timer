package com.example.sleep_timer

import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Context
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private val TAG = "SleepTimerNLS"

    companion object {
        private var instance: NotificationListener? = null
        
        fun pauseAllMedia(context: Context) {
            Log.d("SleepTimerNLS", "=== pauseAllMedia called ===")
            
            if (instance == null) {
                Log.e("SleepTimerNLS", "ERROR: NotificationListener instance is NULL!")
                Log.e("SleepTimerNLS", "User likely hasn't granted Notification Listener permission")
                Log.e("SleepTimerNLS", "Media can only be muted, not paused without this permission")
            } else {
                Log.d("SleepTimerNLS", "NotificationListener instance available, attempting pause...")
                instance?.pauseMediaSessions()
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "=== NotificationListener CONNECTED ===")
        Log.d(TAG, "Media control is now available")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "=== NotificationListener DISCONNECTED ===")
    }

    fun pauseMediaSessions() {
        try {
            Log.d(TAG, "--- Starting pauseMediaSessions() ---")
            
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val activeSessions = mediaSessionManager.getActiveSessions(null)
            
            Log.d(TAG, "Found ${activeSessions.size} active media sessions")
            
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "No active media sessions found!")
                Log.w(TAG, "Either no media is playing, or permission is insufficient")
                return
            }
            
            for ((index, controller) in activeSessions.withIndex()) {
                try {
                    val packageName = controller.packageName
                    val playbackState = controller.playbackState
                    val playbackInfo = controller.playbackInfo
                    
                    Log.d(TAG, "--- Session $index: $packageName ---")
                    
                    if (playbackState != null) {
                        val state = when (playbackState.state) {
                            PlaybackState.STATE_PLAYING -> "PLAYING"
                            PlaybackState.STATE_PAUSED -> "PAUSED"
                            PlaybackState.STATE_STOPPED -> "STOPPED"
                            PlaybackState.STATE_BUFFERING -> "BUFFERING"
                            else -> "OTHER (${playbackState.state})"
                        }
                        Log.d(TAG, "  Current state: $state")
                        Log.d(TAG, "  Position: ${playbackState.position}ms")
                        
                        // Only pause if currently playing
                        if (playbackState.state == PlaybackState.STATE_PLAYING) {
                            Log.d(TAG, "  >>> Sending PAUSE command")
                            controller.transportControls.pause()
                            
                            // Also send stop for good measure
                            Log.d(TAG, "  >>> Sending STOP command")
                            controller.transportControls.stop()
                            
                            // Send seekTo(0) to reset position
                            Log.d(TAG, "  >>> Sending SEEK_TO(0) command")
                            controller.transportControls.seekTo(0)
                            
                            Log.d(TAG, "  ✓ Commands sent successfully to $packageName")
                        } else {
                            Log.d(TAG, "  → Already paused/stopped, skipping")
                        }
                    } else {
                        Log.w(TAG, "  PlaybackState is null for $packageName")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "  Error controlling session: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            Log.d(TAG, "--- pauseMediaSessions() completed ---")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
            Log.e(TAG, "Notification Listener permission may be insufficient!")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in pauseMediaSessions: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Log media-related notifications
        if (sbn.notification.extras.containsKey("android.mediaSession")) {
            Log.d(TAG, "Media notification posted from: ${sbn.packageName}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not used for now
    }
}

