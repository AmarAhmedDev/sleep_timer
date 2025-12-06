import 'package:flutter/material.dart';

class AppGradients {
  // Main background gradient - Purple to Blue
  static const LinearGradient backgroundGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF6B4CE6), // Purple
      Color(0xFF4E54C8), // Deep Blue
      Color(0xFF2E3192), // Darker Blue
    ],
    stops: [0.0, 0.5, 1.0],
  );

  // Circular countdown gradient
  static const LinearGradient circleGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF00F5FF), // Cyan
      Color(0xFF0080FF), // Bright Blue
      Color(0xFF6B4CE6), // Purple
    ],
    stops: [0.0, 0.5, 1.0],
  );

  // Start button gradient
  static const LinearGradient buttonGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF00F5FF), // Cyan
      Color(0xFF0080FF), // Bright Blue
    ],
  );

  // Stop button gradient
  static const LinearGradient stopButtonGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFFFF6B9D), // Pink
      Color(0xFFC239B3), // Purple-Pink
    ],
  );

  // Card gradient for onboarding
  static const LinearGradient cardGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0x40FFFFFF), // Semi-transparent white
      Color(0x20FFFFFF), // More transparent
    ],
  );

  // Shimmer effect for loading
  static const LinearGradient shimmerGradient = LinearGradient(
    begin: Alignment(-1.0, 0.0),
    end: Alignment(1.0, 0.0),
    colors: [Color(0x00FFFFFF), Color(0x40FFFFFF), Color(0x00FFFFFF)],
    stops: [0.0, 0.5, 1.0],
  );
}
