import 'package:flutter_local_notifications/flutter_local_notifications.dart';

class NotificationService {
  static final FlutterLocalNotificationsPlugin _notifications =
      FlutterLocalNotificationsPlugin();

  static Future<void> initialize() async {
    const androidSettings = AndroidInitializationSettings(
      '@mipmap/ic_launcher',
    );
    const iosSettings = DarwinInitializationSettings();

    const settings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _notifications.initialize(settings);
  }

  static Future<void> showNotification(String title, String body) async {
    const androidDetails = AndroidNotificationDetails(
      'sleep_timer_main',
      'Sleep Timer Notifications',
      channelDescription: 'Notifications for sleep timer events',
      importance: Importance.high,
      priority: Priority.high,
      playSound: true,
      enableVibration: true,
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
      'sleep_timer_ongoing',
      'Active Timer',
      channelDescription: 'Shows active sleep timer countdown',
      importance: Importance.low,
      priority: Priority.low,
      ongoing: true,
      autoCancel: false,
      showWhen: false,
    );

    const details = NotificationDetails(android: androidDetails);

    await _notifications.show(
      1,
      'Sleep Timer Active',
      timeStr + ' remaining',
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
