package com.example.sleep_timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TileTimerReceiver : BroadcastReceiver() {
    private val TAG = "TileTimerReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Timer expired! Triggering sleep actions...")
        
        // Clear timer state
        val prefs = context.getSharedPreferences("sleep_timer_tile", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_timer_active", false)
            putLong("remaining_time_ms", 0)
            putLong("start_time_ms", 0)
            apply()
        }
        
        // Trigger sleep actions (media pause and screen lock)
        val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        mainIntent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("trigger_sleep_actions", true)
        }
        context.startActivity(mainIntent)
        
        Log.d(TAG, "Sleep actions triggered")
    }
}
