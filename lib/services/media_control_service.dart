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
}
