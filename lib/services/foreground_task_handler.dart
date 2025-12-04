import 'package:flutter_foreground_task/flutter_foreground_task.dart';

@pragma('vm:entry-point')
void startCallback() {
  FlutterForegroundTask.setTaskHandler(TimerTaskHandler());
}

class TimerTaskHandler extends TaskHandler {
  int remainingSeconds = 0;

  @override
  Future<void> onStart(DateTime timestamp, TaskStarter starter) async {
    print('Timer task started');
  }

  @override
  void onRepeatEvent(DateTime timestamp) {
    if (remainingSeconds > 0) {
      remainingSeconds--;

      FlutterForegroundTask.updateService(
        notificationTitle: 'Sleep Timer Running',
        notificationText: _formatTime(remainingSeconds),
      );

      // Send data to main isolate
      FlutterForegroundTask.sendDataToMain({
        'remainingSeconds': remainingSeconds,
      });

      if (remainingSeconds == 0) {
        FlutterForegroundTask.sendDataToMain({'action': 'timer_complete'});
      }
    }
  }

  @override
  Future<void> onDestroy(DateTime timestamp) async {
    print('Timer task destroyed');
  }

  String _formatTime(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    final secs = seconds % 60;

    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    }
    return '${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
  }
}
