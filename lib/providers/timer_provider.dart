import 'dart:async';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/timer_history.dart';
import '../services/media_control_service.dart';
import '../services/notification_service.dart';

class TimerProvider extends ChangeNotifier {
  int _remainingSeconds = 0;
  bool _isRunning = false;
  Timer? _timer;
  DateTime? _startTime;
  int _initialSeconds = 0;

  int get remainingSeconds => _remainingSeconds;
  bool get isRunning => _isRunning;
  int get initialSeconds => _initialSeconds;

  String get formattedTime {
    final hours = _remainingSeconds ~/ 3600;
    final minutes = (_remainingSeconds % 3600) ~/ 60;
    final seconds = _remainingSeconds % 60;

    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    }
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  void startTimer(int seconds) {
    _initialSeconds = seconds;
    _remainingSeconds = seconds;
    _isRunning = true;
    _startTime = DateTime.now();

    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remainingSeconds > 0) {
        _remainingSeconds--;

        // Show notification 1 minute before expiry
        if (_remainingSeconds == 60) {
          NotificationService.showNotification(
            'Timer Alert',
            'Your timer will expire in 1 minute',
          );
        }

        notifyListeners();
      } else {
        _onTimerComplete();
      }
    });

    notifyListeners();
  }

  void pauseTimer() {
    _timer?.cancel();
    _isRunning = false;
    notifyListeners();
  }

  void resumeTimer() {
    if (_remainingSeconds > 0) {
      _isRunning = true;
      startTimer(_remainingSeconds);
    }
  }

  void stopTimer() {
    _timer?.cancel();
    _remainingSeconds = 0;
    _isRunning = false;
    _initialSeconds = 0;
    notifyListeners();
  }

  Future<void> _onTimerComplete() async {
    print('=== TIMER COMPLETE - STOPPING MEDIA ===');
    _timer?.cancel();
    _isRunning = false;

    // Save to history
    await _saveToHistory();
    print('History saved');

    // Try vibrating first to test native connection
    print('Testing native connection with vibrate...');
    try {
      await MediaControlService.vibrateDevice();
      print('Vibrate call completed');
    } catch (e) {
      print('Vibrate error: $e');
    }

    // Stop media and close apps - THIS IS THE KEY CALL
    print('Calling MediaControlService.stopMediaAndCloseApps()...');
    try {
      await MediaControlService.stopMediaAndCloseApps();
      print('MediaControlService call completed');
    } catch (e) {
      print('Error calling MediaControlService: $e');
    }

    // Show completion notification
    print('Showing notification...');
    await NotificationService.showNotification(
      'Sleep Timer Complete',
      'Media has been stopped. Sweet dreams!',
    );

    // Lock screen / go home
    print('Calling lockScreen...');
    await MediaControlService.lockScreen();

    // Try vibrate again at the end
    try {
      await MediaControlService.vibrateDevice();
    } catch (e) {
      print('Final vibrate error: $e');
    }

    notifyListeners();
    print('=== TIMER COMPLETE SEQUENCE FINISHED ===');
  }

  Future<void> _saveToHistory() async {
    final prefs = await SharedPreferences.getInstance();
    final history = await getHistory();

    final newEntry = TimerHistory(
      duration: _initialSeconds,
      startTime: _startTime ?? DateTime.now(),
      endTime: DateTime.now(),
    );

    history.insert(0, newEntry);

    // Keep only last 50 entries
    if (history.length > 50) {
      history.removeRange(50, history.length);
    }

    final historyJson = history.map((e) => e.toJson()).toList();
    await prefs.setString('timer_history', historyJson.toString());
  }

  Future<List<TimerHistory>> getHistory() async {
    final prefs = await SharedPreferences.getInstance();
    final historyString = prefs.getString('timer_history');

    if (historyString == null) return [];

    // Parse history (simplified)
    return [];
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
