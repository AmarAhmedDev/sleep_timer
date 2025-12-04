import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsProvider extends ChangeNotifier {
  bool _isDarkMode = true;
  bool _vibrationEnabled = true;
  bool _soundEnabled = true;
  bool _autoStopEnabled = true;
  bool _screenOffEnabled = true;
  bool _noSleepMode = false;
  String _stopMethod = 'force_close'; // 'force_close', 'mute', 'pause'

  bool get isDarkMode => _isDarkMode;
  bool get vibrationEnabled => _vibrationEnabled;
  bool get soundEnabled => _soundEnabled;
  bool get autoStopEnabled => _autoStopEnabled;
  bool get screenOffEnabled => _screenOffEnabled;
  bool get noSleepMode => _noSleepMode;
  String get stopMethod => _stopMethod;

  SettingsProvider() {
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    _isDarkMode = prefs.getBool('dark_mode') ?? true;
    _vibrationEnabled = prefs.getBool('vibration') ?? true;
    _soundEnabled = prefs.getBool('sound') ?? true;
    _autoStopEnabled = prefs.getBool('auto_stop') ?? true;
    _screenOffEnabled = prefs.getBool('screen_off') ?? true;
    _noSleepMode = prefs.getBool('no_sleep_mode') ?? false;
    _stopMethod = prefs.getString('stop_method') ?? 'force_close';
    notifyListeners();
  }

  Future<void> toggleDarkMode() async {
    _isDarkMode = !_isDarkMode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('dark_mode', _isDarkMode);
    notifyListeners();
  }

  Future<void> toggleVibration() async {
    _vibrationEnabled = !_vibrationEnabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('vibration', _vibrationEnabled);
    notifyListeners();
  }

  Future<void> toggleSound() async {
    _soundEnabled = !_soundEnabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('sound', _soundEnabled);
    notifyListeners();
  }

  Future<void> toggleAutoStop() async {
    _autoStopEnabled = !_autoStopEnabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('auto_stop', _autoStopEnabled);
    notifyListeners();
  }

  Future<void> toggleScreenOff() async {
    _screenOffEnabled = !_screenOffEnabled;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('screen_off', _screenOffEnabled);
    notifyListeners();
  }

  Future<void> toggleNoSleepMode() async {
    _noSleepMode = !_noSleepMode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('no_sleep_mode', _noSleepMode);
    notifyListeners();
  }

  Future<void> setStopMethod(String method) async {
    _stopMethod = method;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('stop_method', method);
    notifyListeners();
  }
}
