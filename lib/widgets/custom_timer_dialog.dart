import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../providers/timer_provider.dart';

class CustomTimerDialog extends StatefulWidget {
  const CustomTimerDialog({super.key});

  @override
  State<CustomTimerDialog> createState() => _CustomTimerDialogState();
}

class _CustomTimerDialogState extends State<CustomTimerDialog> {
  final _hoursController = TextEditingController(text: '0');
  final _minutesController = TextEditingController(text: '0');

  @override
  void dispose() {
    _hoursController.dispose();
    _minutesController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Custom Timer'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _hoursController,
                  keyboardType: TextInputType.number,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  decoration: const InputDecoration(
                    labelText: 'Hours',
                    border: OutlineInputBorder(),
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: TextField(
                  controller: _minutesController,
                  keyboardType: TextInputType.number,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  decoration: const InputDecoration(
                    labelText: 'Minutes',
                    border: OutlineInputBorder(),
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
            ],
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: () {
            final hours = int.tryParse(_hoursController.text) ?? 0;
            final minutes = int.tryParse(_minutesController.text) ?? 0;
            final totalSeconds = (hours * 3600) + (minutes * 60);

            if (totalSeconds > 0) {
              context.read<TimerProvider>().startTimer(totalSeconds);
              Navigator.pop(context);
            } else {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Please enter a valid time')),
              );
            }
          },
          child: const Text('Start'),
        ),
      ],
    );
  }
}
