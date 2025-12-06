import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'media_control_service.dart';

class PermissionService {
  static Future<void> requestAllPermissions(BuildContext context) async {
    // Request notification permission
    await Permission.notification.request();

    // Request system alert window (overlay) permission
    if (await Permission.systemAlertWindow.isDenied) {
      await Permission.systemAlertWindow.request();
    }

    // Request ignore battery optimizations
    if (await Permission.ignoreBatteryOptimizations.isDenied) {
      await Permission.ignoreBatteryOptimizations.request();
    }

    // Request Notification Listener access (for media control)
    await _requestNotificationListenerPermission(context);

    // Request Device Admin (for screen lock)
    await _requestDeviceAdminPermission(context);
  }

  static Future<void> _requestNotificationListenerPermission(
    BuildContext context,
  ) async {
    final isEnabled = await MediaControlService.isNotificationListenerEnabled();

    if (!isEnabled) {
      // Show explanation dialog
      final shouldRequest = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Enable Media Control'),
          content: const Text(
            'Sleep Timer needs Notification Listener access to pause media playback from other apps (YouTube, Spotify, etc.) when the timer expires.\n\n'
            'Tap "Enable" to open settings, then find and enable "Smart Sleep Timer".',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Skip'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Enable'),
            ),
          ],
        ),
      );

      if (shouldRequest == true) {
        await MediaControlService.requestNotificationListener();
      }
    }
  }

  static Future<void> _requestDeviceAdminPermission(
    BuildContext context,
  ) async {
    final isActive = await MediaControlService.isDeviceAdminActive();

    if (!isActive) {
      // Show explanation dialog
      final shouldRequest = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Enable Screen Lock'),
          content: const Text(
            'Sleep Timer needs Device Admin permission to turn off your screen when the timer expires.\n\n'
            'This helps save battery and ensures complete silence. You can remove this permission anytime from your device settings.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Skip'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Grant Permission'),
            ),
          ],
        ),
      );

      if (shouldRequest == true) {
        await MediaControlService.requestDeviceAdmin();
      }
    }
  }

  static Future<bool> hasAllPermissions() async {
    final notification = await Permission.notification.isGranted;
    final overlay = await Permission.systemAlertWindow.isGranted;
    final battery = await Permission.ignoreBatteryOptimizations.isGranted;
    final deviceAdmin = await MediaControlService.isDeviceAdminActive();
    final notificationListener =
        await MediaControlService.isNotificationListenerEnabled();

    return notification &&
        overlay &&
        battery &&
        deviceAdmin &&
        notificationListener;
  }

  static Future<void> openSettings() async {
    await openAppSettings();
  }
}
