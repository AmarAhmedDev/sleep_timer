import 'package:flutter/material.dart';

class AppGradients {
  // Main background gradient - Dark Purple to deep dark violet
  static const LinearGradient backgroundGradient = LinearGradient(
    begin: Alignment.topRight,
    end: Alignment.bottomLeft,
    colors: [
      // Color(0xFF2E094E), // Lighter dark purple at top
      // Color(0xFF130327), // Deep dark violet
      // Color(0xFF0D021C), // Almost black violet
      Color.fromARGB(255, 0, 0, 0),
      Color.fromARGB(255, 0, 0, 0),
      Color.fromARGB(255, 0, 0, 0),
    ],
    stops: [0.0, 0.5, 1.0],
  );

  // Circular countdown gradient
  static const LinearGradient circleGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFFFF00D4), // Neon Magenta
      Color(0xFF9400FF), // Neon Purple
    ],
  );

  // Start button gradient
  static const LinearGradient buttonGradient = LinearGradient(
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
    colors: [
      Color(0xFFFF00D4), // Neon Magenta
      Color(0xFF9400FF), // Neon Purple
    ],
  );

  // Stop button gradient
  static const LinearGradient stopButtonGradient = LinearGradient(
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
    colors: [
      Color(0xFF320857), // Dark grey-purple
      Color(0xFF1E0438), // Darker grey-purple
    ],
  );

  // Glass card gradient
  static const LinearGradient cardGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0x33FFFFFF), // Semi-transparent white
      Color(0x0AFFFFFF), // More transparent
    ],
  );

  // Shimmer effect for loading
  static const LinearGradient shimmerGradient = LinearGradient(
    begin: Alignment(-1.0, 0.0),
    end: Alignment(1.0, 0.0),
    colors: [Color(0x00FFFFFF), Color(0x33FFFFFF), Color(0x00FFFFFF)],
    stops: [0.0, 0.5, 1.0],
  );
}
