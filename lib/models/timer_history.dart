class TimerHistory {
  final int duration;
  final DateTime startTime;
  final DateTime endTime;

  TimerHistory({
    required this.duration,
    required this.startTime,
    required this.endTime,
  });

  String get formattedDuration {
    final hours = duration ~/ 3600;
    final minutes = (duration % 3600) ~/ 60;

    if (hours > 0) {
      return '$hours hr ${minutes} min';
    }
    return '$minutes min';
  }

  Map<String, dynamic> toJson() {
    return {
      'duration': duration,
      'startTime': startTime.toIso8601String(),
      'endTime': endTime.toIso8601String(),
    };
  }

  factory TimerHistory.fromJson(Map<String, dynamic> json) {
    return TimerHistory(
      duration: json['duration'],
      startTime: DateTime.parse(json['startTime']),
      endTime: DateTime.parse(json['endTime']),
    );
  }
}
