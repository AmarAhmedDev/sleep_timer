package com.sleeptimer.app

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private val TAG = "SleepTimerNLS"

    companion object {
        private var instance: NotificationListener? = null
        
        fun getInstance(): NotificationListener? = instance
        
        fun isConnected(): Boolean = instance != null
        
        /**
         * Static method to pause all media sessions.
         * Can be called from anywhere in the app.
         */
        fun pauseAllMedia(context: Context) {
            Log.d("SleepTimerNLS", "=== pauseAllMedia called ===")
            
            if (instance == null) {
                Log.e("SleepTimerNLS", "ERROR: NotificationListener instance is NULL!")
                Log.e("SleepTimerNLS", "User likely hasn't granted Notification Listener permission")
                Log.e("SleepTimerNLS", "Media pause via MediaSessionManager not available")
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
        Log.d(TAG, "Media control via MediaSessionManager is now available")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "=== NotificationListener DISCONNECTED ===")
    }

    /**
     * Pauses all active media sessions.
     * This is the most reliable way to pause audio apps like Spotify, YouTube Music, etc.
     */
    fun pauseMediaSessions() {
        try {
            Log.d(TAG, "--- Starting pauseMediaSessions() ---")
            
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val componentName = ComponentName(this, NotificationListener::class.java)
            
            val activeSessions: List<MediaController>
            try {
                activeSessions = mediaSessionManager.getActiveSessions(componentName)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException: Cannot access media sessions")
                Log.e(TAG, "This usually means Notification Listener permission was revoked")
                return
            }
            
            Log.d(TAG, "Found ${activeSessions.size} active media session(s)")
            
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "No active media sessions found!")
                Log.w(TAG, "Either no media is playing, or permission is insufficient")
                return
            }
            
            for ((index, controller) in activeSessions.withIndex()) {
                try {
                    val packageName = controller.packageName
                    val playbackState = controller.playbackState
                    
                    Log.d(TAG, "--- Session $index: $packageName ---")
                    
                    if (playbackState != null) {
                        val stateName = when (playbackState.state) {
                            PlaybackState.STATE_PLAYING -> "PLAYING"
                            PlaybackState.STATE_PAUSED -> "PAUSED"
                            PlaybackState.STATE_STOPPED -> "STOPPED"
                            PlaybackState.STATE_BUFFERING -> "BUFFERING"
                            else -> "OTHER (${playbackState.state})"
                        }
                        Log.d(TAG, "  Current state: $stateName")
                        Log.d(TAG, "  Position: ${playbackState.position}ms")
                        
                        // Send pause command
                        Log.d(TAG, "  >>> Sending PAUSE command")
                        controller.transportControls.pause()
                        
                        // Also send stop for good measure
                        Log.d(TAG, "  >>> Sending STOP command")
                        controller.transportControls.stop()
                        
                        Log.d(TAG, "  ✓ Commands sent successfully to $packageName")
                    } else {
                        Log.w(TAG, "  PlaybackState is null for $packageName, attempting pause anyway")
                        controller.transportControls.pause()
                        controller.transportControls.stop()
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

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Log media-related notifications for debugging
        sbn?.let {
            if (it.notification.extras.containsKey("android.mediaSession")) {
                Log.d(TAG, "Media notification posted from: ${it.packageName}")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Required override - not used
    }
}
