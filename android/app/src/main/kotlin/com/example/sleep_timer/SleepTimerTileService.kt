package com.example.sleep_timer

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SleepTimerTileService : TileService() {
    private val TAG = "SleepTimerTile"

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile clicked")

        // Check if Notification Listener is enabled
        val isNotificationListenerEnabled = isNotificationListenerEnabled()
        
        when {
            !isNotificationListenerEnabled -> {
                // Open Notification Listener settings
                Log.d(TAG, "Opening Notification Listener settings")
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityAndCollapse(intent)
                
                showDialog(createAlertDialog())
            }
            else -> {
                // Open the app to start timer
                Log.d(TAG, "Opening app to start timer")
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityAndCollapse(intent)
            }
        }
    }

    private fun createAlertDialog(): android.app.AlertDialog {
        return android.app.AlertDialog.Builder(this)
            .setTitle("Enable Media Control")
            .setMessage("Please enable 'Smart Sleep Timer' in the Notification Access settings to allow the app to pause media playback.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        val isNotificationListenerEnabled = isNotificationListenerEnabled()
        
        if (isNotificationListenerEnabled) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Sleep Timer"
            tile.subtitle = "Tap to start"
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Sleep Timer"
            tile.subtitle = "Enable permission"
        }
        
        tile.updateTile()
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
}
