import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:math' as math;
import '../providers/timer_provider.dart';

class TimerDisplay extends StatelessWidget {
  const TimerDisplay({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<TimerProvider>(
      builder: (context, timer, _) {
        final progress = timer.initialSeconds > 0
            ? timer.remainingSeconds / timer.initialSeconds
            : 0.0;

        return Center(
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Animated circle
              SizedBox(
                width: 280,
                height: 280,
                child: CustomPaint(
                  painter: TimerCirclePainter(
                    progress: progress,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ),
              // Timer text
              Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    Icons.bedtime,
                    size: 48,
                    color: Theme.of(
                      context,
                    ).colorScheme.primary.withOpacity(0.7),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    timer.formattedTime,
                    style: Theme.of(context).textTheme.displayLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      fontSize: 56,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    timer.isRunning ? 'Running...' : 'Paused',
                    style: Theme.of(
                      context,
                    ).textTheme.titleMedium?.copyWith(color: Colors.grey[600]),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }
}

class TimerCirclePainter extends CustomPainter {
  final double progress;
  final Color color;

  TimerCirclePainter({required this.progress, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;

    // Background circle
    final bgPaint = Paint()
      ..color = color.withOpacity(0.1)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 12;
    canvas.drawCircle(center, radius, bgPaint);

    // Progress arc
    final progressPaint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 12
      ..strokeCap = StrokeCap.round;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      -math.pi / 2,
      2 * math.pi * progress,
      false,
      progressPaint,
    );
  }

  @override
  bool shouldRepaint(TimerCirclePainter oldDelegate) {
    return oldDelegate.progress != progress;
  }
}
