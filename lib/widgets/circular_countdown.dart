import 'package:flutter/material.dart';
import 'dart:math' as math;

class CircularCountdown extends StatefulWidget {
  final int totalSeconds;
  final int remainingSeconds;
  final VoidCallback? onComplete;

  const CircularCountdown({
    Key? key,
    required this.totalSeconds,
    required this.remainingSeconds,
    this.onComplete,
  }) : super(key: key);

  @override
  State<CircularCountdown> createState() => _CircularCountdownState();
}

class _CircularCountdownState extends State<CircularCountdown>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );
  }

  @override
  void didUpdateWidget(CircularCountdown oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.remainingSeconds != widget.remainingSeconds) {
      _controller.forward(from: 0.0);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  String _formatTime(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    final secs = seconds % 60;

    if (hours > 0) {
      return '${hours}h ${minutes}m';
    } else if (minutes > 0) {
      return '${minutes}m ${secs}s';
    } else {
      return '${secs}s';
    }
  }

  @override
  Widget build(BuildContext context) {
    final progress = widget.totalSeconds > 0
        ? widget.remainingSeconds / widget.totalSeconds
        : 0.0;

    return SizedBox(
      width: 280,
      height: 280,
      child: CustomPaint(
        painter: CircularCountdownPainter(
          progress: progress,
          animation: _controller,
        ),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _formatTime(widget.remainingSeconds),
                style: const TextStyle(
                  fontSize: 48,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                  letterSpacing: 2,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                widget.remainingSeconds > 0 ? 'Remaining' : 'Complete',
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.white.withOpacity(0.7),
                  letterSpacing: 1,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class CircularCountdownPainter extends CustomPainter {
  final double progress;
  final Animation<double> animation;

  CircularCountdownPainter({required this.progress, required this.animation})
    : super(repaint: animation);

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 20;

    // Background circle
    final bgPaint = Paint()
      ..color = Colors.white.withOpacity(0.1)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 20
      ..strokeCap = StrokeCap.round;

    canvas.drawCircle(center, radius, bgPaint);

    // Progress circle with gradient
    final rect = Rect.fromCircle(center: center, radius: radius);
    final gradient = SweepGradient(
      startAngle: -math.pi / 2,
      endAngle: 3 * math.pi / 2,
      colors: const [
        Color(0xFF00F5FF), // Cyan
        Color(0xFF0080FF), // Blue
        Color(0xFF6B4CE6), // Purple
        Color(0xFF00F5FF), // Cyan (loop)
      ],
      stops: const [0.0, 0.33, 0.66, 1.0],
    );

    final progressPaint = Paint()
      ..shader = gradient.createShader(rect)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 20
      ..strokeCap = StrokeCap.round;

    final sweepAngle = 2 * math.pi * progress;
    canvas.drawArc(rect, -math.pi / 2, sweepAngle, false, progressPaint);

    // Animated glow effect
    if (animation.value > 0) {
      final glowPaint = Paint()
        ..color = Colors.white.withOpacity(0.3 * (1 - animation.value))
        ..style = PaintingStyle.stroke
        ..strokeWidth = 25 + (10 * animation.value)
        ..strokeCap = StrokeCap.round;

      canvas.drawArc(rect, -math.pi / 2, sweepAngle, false, glowPaint);
    }
  }

  @override
  bool shouldRepaint(CircularCountdownPainter oldDelegate) {
    return oldDelegate.progress != progress ||
        oldDelegate.animation != animation;
  }
}
