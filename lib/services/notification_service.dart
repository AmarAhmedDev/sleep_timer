import 'package:flutter_local_notifications/flutter_local_notifications.dart';

class NotificationService {
  static final FlutterLocalNotificationsPlugin _notifications =
      FlutterLocalNotificationsPlugin();

  static Future<void> initialize() async {
    const androidSettings = AndroidInitializationSettings(
      '@mipmap/launcher_icon',
    );
    const iosSettings = DarwinInitializationSettings();

    const settings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    try {
      await _notifications.initialize(settings);
    
      // Explicitly request notification permissions on Android 13+ through the exact plugin implementation
      await _notifications
          .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>()
          ?.requestNotificationsPermission();
    } catch (e) {
      print('Notification Init Error: $e');
    }
  }

  static Future<void> showNotification(String title, String body) async {
    const androidDetails = AndroidNotificationDetails(
      'sleep_timer_main',
      'Sleep Timer Notifications',
      channelDescription: 'Notifications for sleep timer events',
      importance: Importance.low,
      priority: Priority.low,
      playSound: false,
      enableVibration: false,
      silent: true,
    );

    const iosDetails = DarwinNotificationDetails();

    const details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _notifications.show(0, title, body, details);
  }

  // Show notification when timer starts
  static Future<void> showTimerStartedNotification({
    required int hours,
    required int minutes,
  }) async {
    String timeStr = '';
    if (hours > 0) timeStr += '${hours}h ';
    if (minutes > 0) timeStr += '${minutes}m';

    await showNotification('Sleep Timer Started', 'Timer set for $timeStr');
  }

  // Update ongoing notification with countdown
  static Future<void> updateOngoingNotification({
    required int remainingSeconds,
  }) async {
    final hours = remainingSeconds ~/ 3600;
    final minutes = (remainingSeconds % 3600) ~/ 60;
    final seconds = remainingSeconds % 60;

    String timeStr = '';
    if (hours > 0) timeStr += '${hours}h ';
    if (minutes > 0) timeStr += '${minutes}m ';
    timeStr += '${seconds}s';

    const androidDetails = AndroidNotificationDetails(
      'sleep_timer_ongoing_v2',
      'Active Timer',
      channelDescription: 'Shows active sleep timer countdown',
      importance: Importance.low,
      priority: Priority.low,
      ongoing: true,
      autoCancel: false,
      showWhen: false,
      onlyAlertOnce: true,
      playSound: false,
      enableVibration: false,
      silent: true,
    );

    const details = NotificationDetails(android: androidDetails);

    await _notifications.show(
      1,
      'Sleep Timer Active',
      timeStr,
      details,
    );
  }

  // Cancel ongoing notification
  static Future<void> cancelOngoingNotification() async {
    await _notifications.cancel(1);
  }

  // Show completion notification
  static Future<void> showTimerCompletedNotification() async {
    await showNotification(
      'Sleep Timer Complete',
      'Timer expired. Media paused and screen locked.',
    );
  }
}
