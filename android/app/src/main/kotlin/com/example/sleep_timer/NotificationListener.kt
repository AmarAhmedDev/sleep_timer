package com.example.sleep_timer

import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Context
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private val TAG = "SleepTimerNLS"

    companion object {
        private var instance: NotificationListener? = null
        
        fun pauseAllMedia(context: Context) {
            Log.d("SleepTimerNLS", "pauseAllMedia called")
            instance?.pauseMediaSessions() ?: run {
                Log.w("SleepTimerNLS", "NotificationListener instance not available")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "NotificationListener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "NotificationListener disconnected")
    }

    fun pauseMediaSessions() {
        try {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val activeSessions = mediaSessionManager.getActiveSessions(null)
            
            Log.d(TAG, "Found ${activeSessions.size} active media sessions")
            
            for (controller in activeSessions) {
                try {
                    val playbackState = controller.playbackState
                    if (playbackState != null) {
                        Log.d(TAG, "Session: ${controller.packageName}, State: ${playbackState.state}")
                        
                        // Send pause command
                        controller.transportControls.pause()
                        
                        // Also try stop for good measure
                        controller.transportControls.stop()
                        
                        Log.d(TAG, "Sent pause/stop to ${controller.packageName}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error pausing session: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing media sessions: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Not used for now, but required to override
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not used for now, but required to override
    }
}
