import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/timer_provider.dart';
import '../models/timer_history.dart';
import 'package:intl/intl.dart';

class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Timer History'), centerTitle: true),
      body: FutureBuilder<List<TimerHistory>>(
        future: context.read<TimerProvider>().getHistory(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.history, size: 80, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  Text(
                    'No timer history yet',
                    style: Theme.of(
                      context,
                    ).textTheme.titleLarge?.copyWith(color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Start a timer to see it here',
                    style: Theme.of(
                      context,
                    ).textTheme.bodyMedium?.copyWith(color: Colors.grey[500]),
                  ),
                ],
              ),
            );
          }

          final history = snapshot.data!;
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: history.length,
            itemBuilder: (context, index) {
              final entry = history[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: Theme.of(
                      context,
                    ).colorScheme.primaryContainer,
                    child: Icon(
                      Icons.timer,
                      color: Theme.of(context).colorScheme.onPrimaryContainer,
                    ),
                  ),
                  title: Text(entry.formattedDuration),
                  subtitle: Text(
                    DateFormat(
                      'MMM dd, yyyy - hh:mm a',
                    ).format(entry.startTime),
                  ),
                  trailing: Icon(Icons.check_circle, color: Colors.green[400]),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
