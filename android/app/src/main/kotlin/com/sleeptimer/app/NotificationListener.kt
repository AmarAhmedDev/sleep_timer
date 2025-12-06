package com.sleeptimer.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Required override
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Required override
    }
}
