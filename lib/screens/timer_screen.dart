import 'package:flutter/material.dart';
import 'dart:async';
import '../widgets/circular_countdown.dart';
import '../widgets/time_picker_wheel.dart';
import '../theme/gradients.dart';
import '../services/notification_service.dart';
import '../services/media_control_service.dart';
import '../services/permission_service.dart';

class TimerScreen extends StatefulWidget {
  const TimerScreen({Key? key}) : super(key: key);

  @override
  State<TimerScreen> createState() => _TimerScreenState();
}

class _TimerScreenState extends State<TimerScreen> {
  int selectedHours = 2;
  int selectedMinutes = 0;

  bool _isTimerActive = false;
  int _totalSeconds = 0;
  int _remainingSeconds = 0;
  Timer? _countdownTimer;

  @override
  void initState() {
    super.initState();
    _requestPermissionsIfNeeded();
  }

  Future<void> _requestPermissionsIfNeeded() async {
    await PermissionService.requestAllPermissions(context);
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    super.dispose();
  }

  void _startTimer() async {
    if (selectedHours == 0 && selectedMinutes == 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please select a time greater than 0'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() {
      _totalSeconds = (selectedHours * 3600) + (selectedMinutes * 60);
      _remainingSeconds = _totalSeconds;
      _isTimerActive = true;
    });

    // Show notification
    await NotificationService.showTimerStartedNotification(
      hours: selectedHours,
      minutes: selectedMinutes,
    );

    // Start countdown
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remainingSeconds > 0) {
        setState(() {
          _remainingSeconds--;
        });

        // Update notification with remaining time
        NotificationService.updateOngoingNotification(
          remainingSeconds: _remainingSeconds,
        );
      } else {
        _stopTimer();
        _onTimerComplete();
      }
    });
  }

  void _stopTimer() {
    _countdownTimer?.cancel();
    setState(() {
      _isTimerActive = false;
      _remainingSeconds = 0;
    });

    // Cancel notification
    NotificationService.cancelOngoingNotification();
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
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: AppGradients.backgroundGradient,
        ),
        child: SafeArea(
          child: Column(
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(20.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Sleep Timer',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    // Quick Settings hint
                    if (!_isTimerActive)
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              Icons.swipe_down,
                              size: 16,
                              color: Colors.white.withOpacity(0.8),
                            ),
                            const SizedBox(width: 4),
                            Text(
                              'Quick tile',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.white.withOpacity(0.8),
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
              ),

              Expanded(
                child: _isTimerActive
                    ? _buildActiveTimer()
                    : _buildTimerPicker(),
              ),

              // Action Button
              Padding(
                padding: const EdgeInsets.all(24.0),
                child: _buildActionButton(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTimerPicker() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            'Set Sleep Timer',
            style: TextStyle(
              fontSize: 20,
              color: Colors.white70,
              letterSpacing: 1,
            ),
          ),
          const SizedBox(height: 40),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              TimePickerWheel(
                value: selectedHours,
                maxValue: 23,
                label: 'Hours',
                onChanged: (value) {
                  setState(() {
                    selectedHours = value;
                  });
                },
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Text(
                  ':',
                  style: TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                    color: Colors.white.withOpacity(0.5),
                  ),
                ),
              ),
              TimePickerWheel(
                value: selectedMinutes,
                maxValue: 59,
                label: 'Minutes',
                onChanged: (value) {
                  setState(() {
                    selectedMinutes = value;
                  });
                },
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildActiveTimer() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          CircularCountdown(
            totalSeconds: _totalSeconds,
            remainingSeconds: _remainingSeconds,
            onComplete: _onTimerComplete,
          ),
          const SizedBox(height: 40),
          Text(
            'Timer Active',
            style: TextStyle(
              fontSize: 18,
              color: Colors.white.withOpacity(0.7),
              letterSpacing: 1,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButton() {
    final isStartButton = !_isTimerActive;
    final gradient = isStartButton
        ? AppGradients.buttonGradient
        : AppGradients.stopButtonGradient;
    final text = isStartButton ? 'Start Timer' : 'Stop Timer';
    final icon = isStartButton ? Icons.play_arrow : Icons.stop;

    return Container(
      width: double.infinity,
      height: 64,
      decoration: BoxDecoration(
        gradient: gradient,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: isStartButton ? _startTimer : _stopTimer,
          borderRadius: BorderRadius.circular(20),
          child: Center(
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(icon, color: Colors.white, size: 28),
                const SizedBox(width: 12),
                Text(
                  text,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                    letterSpacing: 1,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
