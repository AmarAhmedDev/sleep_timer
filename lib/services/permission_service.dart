import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

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
  }

  static Future<bool> hasAllPermissions() async {
    final notification = await Permission.notification.isGranted;
    final overlay = await Permission.systemAlertWindow.isGranted;
    final battery = await Permission.ignoreBatteryOptimizations.isGranted;

    return notification && overlay && battery;
  }

  static Future<void> openSettings() async {
    await openAppSettings();
  }
}
