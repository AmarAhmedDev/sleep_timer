import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:math' as math;
import 'dart:ui';
import '../providers/timer_provider.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  late AnimationController _starsController;
  late AnimationController _pulseController;

  int _selectedHours = 0;
  int _selectedMinutes = 30;

  FixedExtentScrollController? _hoursController;
  FixedExtentScrollController? _minutesController;

  @override
  void initState() {
    super.initState();
    _starsController = AnimationController(
      duration: const Duration(seconds: 20),
      vsync: this,
    )..repeat();

    _pulseController = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    )..repeat(reverse: true);

    _hoursController = FixedExtentScrollController(initialItem: _selectedHours);
    _minutesController = FixedExtentScrollController(
      initialItem: _selectedMinutes,
    );
  }

  @override
  void dispose() {
    _starsController.dispose();
    _pulseController.dispose();
    _hoursController?.dispose();
    _minutesController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // Cosmic Background
          _buildCosmicBackground(),

          // Animated stars
          AnimatedBuilder(
            animation: _starsController,
            builder: (context, child) {
              return CustomPaint(
                size: Size.infinite,
                painter: StarsPainter(animation: _starsController.value),
              );
            },
          ),

          // Main Content
          SafeArea(
            child: Column(
              children: [
                const SizedBox(height: 20),
                _buildCosmicHeader(),
                const SizedBox(height: 20),
                _buildMainCard(),
                const Spacer(),
                _buildTimePicker(),
                const SizedBox(height: 20),
                _buildStartButton(),
                const SizedBox(height: 30),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCosmicBackground() {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF1a1a2e),
            Color(0xFF16213e),
            Color(0xFF0f0f23),
            Color(0xFF1a1a2e),
          ],
          stops: [0.0, 0.3, 0.7, 1.0],
        ),
      ),
    );
  }

  Widget _buildCosmicHeader() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildCelestialIcon('☽', const Color(0xFFB8B8D0), 50),
          _buildCelestialIcon('☀', const Color(0xFFE8A838), 60),
          _buildCelestialIcon('✧', const Color(0xFFB8B8D0), 50),
        ],
      ),
    );
  }

  Widget _buildCelestialIcon(String symbol, Color color, double size) {
    return AnimatedBuilder(
      animation: _pulseController,
      builder: (context, child) {
        final scale = 1.0 + (_pulseController.value * 0.1);
        return Transform.scale(
          scale: scale,
          child: Container(
            width: size,
            height: size,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: color.withOpacity(0.3),
                  blurRadius: 20,
                  spreadRadius: 5,
                ),
              ],
            ),
            child: Center(
              child: Text(
                symbol,
                style: TextStyle(
                  fontSize: size * 0.7,
                  color: color,
                  shadows: [Shadow(color: color, blurRadius: 10)],
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildMainCard() {
    return Consumer<TimerProvider>(
      builder: (context, timer, _) {
        final isRunning = timer.isRunning || timer.remainingSeconds > 0;

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Container(
            height: 280,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  const Color(0xFF2d2d44).withOpacity(0.8),
                  const Color(0xFF1f1f35).withOpacity(0.9),
                ],
              ),
              border: Border.all(
                color: const Color(0xFF4a4a6a).withOpacity(0.3),
                width: 1,
              ),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF6366f1).withOpacity(0.1),
                  blurRadius: 30,
                  spreadRadius: 5,
                ),
              ],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(24),
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
                child: Stack(
                  children: [
                    // Cosmic decorations inside card
                    Positioned(
                      top: 20,
                      right: 20,
                      child: Container(
                        width: 80,
                        height: 80,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: RadialGradient(
                            colors: [
                              const Color(0xFFE8A838).withOpacity(0.3),
                              Colors.transparent,
                            ],
                          ),
                        ),
                      ),
                    ),
                    Positioned(
                      bottom: 40,
                      left: 30,
                      child: Container(
                        width: 60,
                        height: 60,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: RadialGradient(
                            colors: [
                              const Color(0xFF8B7355).withOpacity(0.3),
                              Colors.transparent,
                            ],
                          ),
                        ),
                      ),
                    ),

                    // Content
                    Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          if (isRunning) ...[
                            // Timer running state
                            AnimatedBuilder(
                              animation: _pulseController,
                              builder: (context, child) {
                                return Text(
                                  timer.formattedTime,
                                  style: TextStyle(
                                    fontSize: 64,
                                    fontWeight: FontWeight.w300,
                                    color: Colors.white,
                                    letterSpacing: 4,
                                    shadows: [
                                      Shadow(
                                        color: const Color(0xFF6366f1)
                                            .withOpacity(
                                              0.5 +
                                                  _pulseController.value * 0.3,
                                            ),
                                        blurRadius: 20,
                                      ),
                                    ],
                                  ),
                                );
                              },
                            ),
                            const SizedBox(height: 16),
                            Text(
                              timer.isRunning ? 'Sleeping Soon...' : 'Paused',
                              style: TextStyle(
                                fontSize: 18,
                                color: Colors.white.withOpacity(0.7),
                                letterSpacing: 2,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Your device will rest peacefully',
                              style: TextStyle(
                                fontSize: 14,
                                color: Colors.white.withOpacity(0.5),
                              ),
                            ),
                            const SizedBox(height: 24),
                            _buildControlButtons(timer),
                          ] else ...[
                            // Welcome state
                            const Text(
                              'Sleep Timer',
                              style: TextStyle(
                                fontSize: 42,
                                fontWeight: FontWeight.w300,
                                color: Colors.white,
                                letterSpacing: 2,
                              ),
                            ),
                            const SizedBox(height: 16),
                            Text(
                              'Set the time and let\nyour mind drift to dreams',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 16,
                                color: Colors.white.withOpacity(0.6),
                                height: 1.5,
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildControlButtons(TimerProvider timer) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        if (timer.isRunning)
          _buildControlButton(
            icon: Icons.pause_rounded,
            label: 'Pause',
            onTap: () => timer.pauseTimer(),
            color: const Color(0xFFE8A838),
          )
        else if (timer.remainingSeconds > 0)
          _buildControlButton(
            icon: Icons.play_arrow_rounded,
            label: 'Resume',
            onTap: () => timer.resumeTimer(),
            color: const Color(0xFF4ADE80),
          ),
        const SizedBox(width: 20),
        _buildControlButton(
          icon: Icons.stop_rounded,
          label: 'Stop',
          onTap: () => timer.stopTimer(),
          color: const Color(0xFFEF4444),
        ),
      ],
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
    required Color color,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          gradient: LinearGradient(
            colors: [color.withOpacity(0.3), color.withOpacity(0.1)],
          ),
          border: Border.all(color: color.withOpacity(0.5)),
        ),
        child: Row(
          children: [
            Icon(icon, color: color, size: 24),
            const SizedBox(width: 8),
            Text(
              label,
              style: TextStyle(
                color: color,
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimePicker() {
    return Consumer<TimerProvider>(
      builder: (context, timer, _) {
        if (timer.isRunning || timer.remainingSeconds > 0) {
          return const SizedBox.shrink();
        }

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  const Color(0xFF2d2d44).withOpacity(0.6),
                  const Color(0xFF1f1f35).withOpacity(0.8),
                ],
              ),
              border: Border.all(
                color: const Color(0xFF4a4a6a).withOpacity(0.3),
                width: 1,
              ),
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      Icons.schedule_rounded,
                      color: const Color(0xFFE8A838),
                      size: 24,
                    ),
                    const SizedBox(width: 12),
                    Text(
                      'Set Sleep Time',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: Colors.white.withOpacity(0.9),
                        letterSpacing: 1,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _buildWheelPicker(
                      label: 'Hours',
                      itemCount: 24,
                      controller: _hoursController!,
                      onChanged: (value) {
                        setState(() => _selectedHours = value);
                      },
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Text(
                        ':',
                        style: TextStyle(
                          fontSize: 48,
                          fontWeight: FontWeight.w200,
                          color: const Color(0xFFE8A838),
                        ),
                      ),
                    ),
                    _buildWheelPicker(
                      label: 'Minutes',
                      itemCount: 60,
                      controller: _minutesController!,
                      onChanged: (value) {
                        setState(() => _selectedMinutes = value);
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildWheelPicker({
    required String label,
    required int itemCount,
    required FixedExtentScrollController controller,
    required ValueChanged<int> onChanged,
  }) {
    return Column(
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: Colors.white.withOpacity(0.5),
            letterSpacing: 2,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          width: 80,
          height: 140,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Colors.transparent,
                const Color(0xFF6366f1).withOpacity(0.1),
                Colors.transparent,
              ],
            ),
          ),
          child: ListWheelScrollView.useDelegate(
            controller: controller,
            itemExtent: 50,
            perspective: 0.005,
            diameterRatio: 1.5,
            physics: const FixedExtentScrollPhysics(),
            onSelectedItemChanged: onChanged,
            childDelegate: ListWheelChildBuilderDelegate(
              childCount: itemCount,
              builder: (context, index) {
                final isSelected =
                    (label == 'Hours' && index == _selectedHours) ||
                    (label == 'Minutes' && index == _selectedMinutes);
                return Center(
                  child: Text(
                    index.toString().padLeft(2, '0'),
                    style: TextStyle(
                      fontSize: isSelected ? 36 : 24,
                      fontWeight: isSelected
                          ? FontWeight.w400
                          : FontWeight.w200,
                      color: isSelected
                          ? Colors.white
                          : Colors.white.withOpacity(0.3),
                    ),
                  ),
                );
              },
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildStartButton() {
    return Consumer<TimerProvider>(
      builder: (context, timer, _) {
        if (timer.isRunning || timer.remainingSeconds > 0) {
          return const SizedBox.shrink();
        }

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 40),
          child: GestureDetector(
            onTap: () {
              final totalSeconds =
                  (_selectedHours * 3600) + (_selectedMinutes * 60);
              if (totalSeconds > 0) {
                timer.startTimer(totalSeconds);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: const Text('Please select a valid time'),
                    backgroundColor: const Color(0xFF2d2d44),
                    behavior: SnackBarBehavior.floating,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                );
              }
            },
            child: AnimatedBuilder(
              animation: _pulseController,
              builder: (context, child) {
                return Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(vertical: 18),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(20),
                    gradient: const LinearGradient(
                      colors: [Color(0xFF6366f1), Color(0xFF8B5CF6)],
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(
                          0xFF6366f1,
                        ).withOpacity(0.3 + _pulseController.value * 0.2),
                        blurRadius: 20,
                        spreadRadius: 2,
                      ),
                    ],
                  ),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.bedtime_rounded,
                        color: Colors.white,
                        size: 28,
                      ),
                      SizedBox(width: 12),
                      Text(
                        'Start Sleep Timer',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                          letterSpacing: 1,
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        );
      },
    );
  }
}

// Custom painter for animated stars
class StarsPainter extends CustomPainter {
  final double animation;
  final List<Star> stars;

  StarsPainter({required this.animation})
    : stars = List.generate(100, (index) => Star.random(index));

  @override
  void paint(Canvas canvas, Size size) {
    for (var star in stars) {
      final twinkle = (math.sin(animation * 2 * math.pi + star.phase) + 1) / 2;
      final paint = Paint()
        ..color = star.color.withOpacity(star.opacity * twinkle)
        ..style = PaintingStyle.fill;

      canvas.drawCircle(
        Offset(star.x * size.width, star.y * size.height),
        star.size,
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(StarsPainter oldDelegate) => true;
}

class Star {
  final double x;
  final double y;
  final double size;
  final double opacity;
  final double phase;
  final Color color;

  Star({
    required this.x,
    required this.y,
    required this.size,
    required this.opacity,
    required this.phase,
    required this.color,
  });

  factory Star.random(int seed) {
    final random = math.Random(seed);
    return Star(
      x: random.nextDouble(),
      y: random.nextDouble(),
      size: random.nextDouble() * 2 + 0.5,
      opacity: random.nextDouble() * 0.5 + 0.3,
      phase: random.nextDouble() * 2 * math.pi,
      color: [
        Colors.white,
        const Color(0xFFE8A838),
        const Color(0xFF6366f1),
      ][random.nextInt(3)],
    );
  }
}
