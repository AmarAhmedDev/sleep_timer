import 'package:flutter/material.dart';

class TimePickerWheel extends StatelessWidget {
  final int value;
  final int maxValue;
  final String label;
  final ValueChanged<int> onChanged;

  const TimePickerWheel({
    Key? key,
    required this.value,
    required this.maxValue,
    required this.label,
    required this.onChanged,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            color: Colors.white.withOpacity(0.7),
            letterSpacing: 1,
          ),
        ),
        const SizedBox(height: 8),
        SizedBox(
          height: 200,
          width: 80,
          child: Stack(
            children: [
              // Selection highlight
              Center(
                child: Container(
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: Colors.white.withOpacity(0.3),
                      width: 2,
                    ),
                  ),
                ),
              ),
              // Scrollable numbers
              ListWheelScrollView.useDelegate(
                controller: FixedExtentScrollController(initialItem: value),
                itemExtent: 50,
                perspective: 0.005,
                diameterRatio: 1.2,
                physics: const FixedExtentScrollPhysics(),
                onSelectedItemChanged: onChanged,
                childDelegate: ListWheelChildBuilderDelegate(
                  childCount: maxValue + 1,
                  builder: (context, index) {
                    final isSelected = index == value;
                    return Center(
                      child: Text(
                        index.toString().padLeft(2, '0'),
                        style: TextStyle(
                          fontSize: isSelected ? 32 : 24,
                          fontWeight: isSelected
                              ? FontWeight.bold
                              : FontWeight.normal,
                          color: isSelected
                              ? Colors.white
                              : Colors.white.withOpacity(0.4),
                        ),
                      ),
                    );
                  },
                ),
              ),
              // Top gradient fade
              Positioned(
                top: 0,
                left: 0,
                right: 0,
                height: 75,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Theme.of(context).scaffoldBackgroundColor,
                        Theme.of(
                          context,
                        ).scaffoldBackgroundColor.withOpacity(0),
                      ],
                    ),
                  ),
                ),
              ),
              // Bottom gradient fade
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                height: 75,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.bottomCenter,
                      end: Alignment.topCenter,
                      colors: [
                        Theme.of(context).scaffoldBackgroundColor,
                        Theme.of(
                          context,
                        ).scaffoldBackgroundColor.withOpacity(0),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
