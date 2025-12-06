import 'package:flutter/services.dart';

class MediaControlService {
  static const platform = MethodChannel('com.sleeptimer/media_control');

  static Future<void> stopMediaAndCloseApps() async {
    print('MediaControlService: Calling stopMediaAndCloseApps...');
    try {
      final result = await platform.invokeMethod('stopMediaAndCloseApps');
      print('MediaControlService: stopMediaAndCloseApps returned: $result');
    } on PlatformException catch (e) {
      print('MediaControlService PlatformException: ${e.code} - ${e.message}');
      print('Details: ${e.details}');
    } catch (e) {
      print('MediaControlService Error: $e');
    }
  }

  static Future<void> lockScreen() async {
    print('MediaControlService: Calling lockScreen...');
    try {
      await platform.invokeMethod('lockScreen');
      print('MediaControlService: lockScreen completed');
    } on PlatformException catch (e) {
      print('MediaControlService lockScreen error: ${e.code} - ${e.message}');
    } catch (e) {
      print('MediaControlService lockScreen error: $e');
    }
  }

  static Future<void> vibrateDevice() async {
    print('MediaControlService: Calling vibrate...');
    try {
      await platform.invokeMethod('vibrate');
      print('MediaControlService: vibrate completed');
    } on PlatformException catch (e) {
      print('MediaControlService vibrate error: ${e.code} - ${e.message}');
    } catch (e) {
      print('MediaControlService vibrate error: $e');
    }
  }

  static Future<String?> getForegroundApp() async {
    try {
      final String? appPackage = await platform.invokeMethod(
        'getForegroundApp',
      );
      return appPackage;
    } catch (e) {
      print('Error getting foreground app: $e');
      return null;
    }
  }

  static Future<bool> isMediaPlaying() async {
    try {
      final bool isPlaying = await platform.invokeMethod('isMediaPlaying');
      return isPlaying;
    } catch (e) {
      print('Error checking media: $e');
      return false;
    }
  }

  // Device Admin methods for screen lock
  static Future<void> requestDeviceAdmin() async {
    print('MediaControlService: Requesting Device Admin...');
    try {
      await platform.invokeMethod('requestDeviceAdmin');
      print('MediaControlService: Device Admin request sent');
    } on PlatformException catch (e) {
      print('MediaControlService Device Admin error: ${e.code} - ${e.message}');
    } catch (e) {
      print('MediaControlService Device Admin error: $e');
    }
  }

  static Future<bool> isDeviceAdminActive() async {
    try {
      final bool isActive = await platform.invokeMethod('isDeviceAdminActive');
      return isActive;
    } catch (e) {
      print('Error checking Device Admin: $e');
      return false;
    }
  }

  // Notification Listener methods for media control
  static Future<void> requestNotificationListener() async {
    print('MediaControlService: Requesting Notification Listener...');
    try {
      await platform.invokeMethod('requestNotificationListener');
      print('MediaControlService: Notification Listener request sent');
    } on PlatformException catch (e) {
      print(
        'MediaControlService Notification Listener error: ${e.code} - ${e.message}',
      );
    } catch (e) {
      print('MediaControlService Notification Listener error: $e');
    }
  }

  static Future<bool> isNotificationListenerEnabled() async {
    try {
      final bool isEnabled = await platform.invokeMethod(
        'checkNotificationListener',
      );
      return isEnabled;
    } catch (e) {
      print('Error checking Notification Listener: $e');
      return false;
    }
  }
}
