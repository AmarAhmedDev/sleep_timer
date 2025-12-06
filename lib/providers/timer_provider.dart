import 'package:flutter/material.dart';
import 'dart:async';
import '../services/notification_service.dart';
import '../services/media_control_service.dart';

class TimerProvider extends ChangeNotifier {
  int _initialSeconds = 0;
  int _remainingSeconds = 0;
  bool _isRunning = false;
  Timer? _timer;

  int get initialSeconds => _initialSeconds;
  int get remainingSeconds => _remainingSeconds;
  bool get isRunning => _isRunning;

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
    if (_isRunning) return;

    _initialSeconds = seconds;
    _remainingSeconds = seconds;
    _isRunning = true;
    notifyListeners();

    // Show notification
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    NotificationService.showTimerStartedNotification(
      hours: hours,
      minutes: minutes,
    );

    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remainingSeconds > 0) {
        _remainingSeconds--;
        notifyListeners();

        // Update notification
        NotificationService.updateOngoingNotification(
          remainingSeconds: _remainingSeconds,
        );
      } else {
        stopTimer();
        _onTimerComplete();
      }
    });
  }

  void stopTimer() {
    _timer?.cancel();
    _isRunning = false;
    _remainingSeconds = 0;
    notifyListeners();

    // Cancel notification
    NotificationService.cancelOngoingNotification();
  }

  void pauseTimer() {
    _timer?.cancel();
    _isRunning = false;
    notifyListeners();
  }

  void resumeTimer() {
    if (_remainingSeconds > 0 && !_isRunning) {
      _isRunning = true;
      notifyListeners();

      _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
        if (_remainingSeconds > 0) {
          _remainingSeconds--;
          notifyListeners();

          // Update notification
          NotificationService.updateOngoingNotification(
            remainingSeconds: _remainingSeconds,
          );
        } else {
          stopTimer();
          _onTimerComplete();
        }
      });
    }
  }

  Future<void> _onTimerComplete() async {
    // Trigger sleep actions
    await MediaControlService.stopMediaAndCloseApps();

    // Show completion notification
    await NotificationService.showTimerCompletedNotification();

    // Vibrate
    await MediaControlService.vibrateDevice();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
