import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsProvider extends ChangeNotifier {
  bool _noSleepMode = false;
  bool _vibrateOnComplete = true;
  bool _showNotifications = true;
  bool _pauseMediaOnComplete = true;

  bool get noSleepMode => _noSleepMode;
  bool get vibrateOnComplete => _vibrateOnComplete;
  bool get showNotifications => _showNotifications;
  bool get pauseMediaOnComplete => _pauseMediaOnComplete;

  SettingsProvider() {
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    _noSleepMode = prefs.getBool('no_sleep_mode') ?? false;
    _vibrateOnComplete = prefs.getBool('vibrate_on_complete') ?? true;
    _showNotifications = prefs.getBool('show_notifications') ?? true;
    _pauseMediaOnComplete = prefs.getBool('pause_media_on_complete') ?? true;
    notifyListeners();
  }

  Future<void> setNoSleepMode(bool value) async {
    _noSleepMode = value;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('no_sleep_mode', value);
  }

  Future<void> setVibrateOnComplete(bool value) async {
    _vibrateOnComplete = value;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('vibrate_on_complete', value);
  }

  Future<void> setShowNotifications(bool value) async {
    _showNotifications = value;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('show_notifications', value);
  }

  Future<void> setPauseMediaOnComplete(bool value) async {
    _pauseMediaOnComplete = value;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('pause_media_on_complete', value);
  }
}
