import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/timer_provider.dart';
import '../providers/settings_provider.dart';
import 'custom_timer_dialog.dart';

class TimerPresets extends StatelessWidget {
  const TimerPresets({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer2<TimerProvider, SettingsProvider>(
      builder: (context, timer, settings, _) {
        final isDisabled =
            timer.isRunning ||
            (settings.noSleepMode && timer.remainingSeconds > 0);

        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Quick Timers',
                style: Theme.of(
                  context,
                ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  _buildPresetButton(context, '5 min', 5 * 60, isDisabled),
                  _buildPresetButton(context, '10 min', 10 * 60, isDisabled),
                  _buildPresetButton(context, '20 min', 20 * 60, isDisabled),
                  _buildPresetButton(context, '30 min', 30 * 60, isDisabled),
                  _buildPresetButton(context, '1 hour', 60 * 60, isDisabled),
                  _buildCustomButton(context, isDisabled),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildPresetButton(
    BuildContext context,
    String label,
    int seconds,
    bool isDisabled,
  ) {
    return ElevatedButton(
      onPressed: isDisabled
          ? null
          : () {
              context.read<TimerProvider>().startTimer(seconds);
            },
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
      child: Text(
        label,
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
      ),
    );
  }

  Widget _buildCustomButton(BuildContext context, bool isDisabled) {
    return ElevatedButton.icon(
      onPressed: isDisabled
          ? null
          : () {
              showDialog(
                context: context,
                builder: (context) => const CustomTimerDialog(),
              );
            },
      icon: const Icon(Icons.edit),
      label: const Text(
        'Custom',
        style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
      ),
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
    );
  }
}
