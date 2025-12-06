import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';
import '../services/permission_service.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings'), centerTitle: true),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildSection(context, 'Appearance', [
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('Dark Mode'),
                  subtitle: const Text('Use dark theme'),
                  value: settings.isDarkMode,
                  onChanged: (_) => settings.toggleDarkMode(),
                  secondary: const Icon(Icons.dark_mode),
                );
              },
            ),
          ]),
          const SizedBox(height: 20),
          _buildSection(context, 'Timer Behavior', [
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('Vibration'),
                  subtitle: const Text('Vibrate when timer ends'),
                  value: settings.vibrationEnabled,
                  onChanged: (_) => settings.toggleVibration(),
                  secondary: const Icon(Icons.vibration),
                );
              },
            ),
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('Sound Alert'),
                  subtitle: const Text('Play sound when timer ends'),
                  value: settings.soundEnabled,
                  onChanged: (_) => settings.toggleSound(),
                  secondary: const Icon(Icons.volume_up),
                );
              },
            ),
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('Screen Off'),
                  subtitle: const Text('Turn off screen when timer ends'),
                  value: settings.screenOffEnabled,
                  onChanged: (_) => settings.toggleScreenOff(),
                  secondary: const Icon(Icons.screen_lock_portrait),
                );
              },
            ),
          ]),
          const SizedBox(height: 20),
          _buildSection(context, 'Quick Settings Tile', [
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return Column(
                  children: [
                    ListTile(
                      title: const Text('Tile Timer Duration'),
                      subtitle: Text('${settings.tileDurationMinutes} minutes'),
                      leading: const Icon(Icons.timer),
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Slider(
                        value: settings.tileDurationMinutes.toDouble(),
                        min: 1,
                        max: 120,
                        divisions: 119,
                        label: '${settings.tileDurationMinutes} min',
                        onChanged: (value) {
                          settings.setTileDuration(value.round());
                        },
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                      child: Text(
                        'Sets the default duration when you tap the Quick Settings tile',
                        style: Theme.of(
                          context,
                        ).textTheme.bodySmall?.copyWith(color: Colors.grey),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ],
                );
              },
            ),
          ]),
          const SizedBox(height: 20),
          _buildSection(context, 'Media Control', [
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('Auto Stop Media'),
                  subtitle: const Text('Automatically stop media apps'),
                  value: settings.autoStopEnabled,
                  onChanged: (_) => settings.toggleAutoStop(),
                  secondary: const Icon(Icons.stop_circle),
                );
              },
            ),
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return ListTile(
                  title: const Text('Stop Method'),
                  subtitle: Text(_getStopMethodLabel(settings.stopMethod)),
                  leading: const Icon(Icons.settings_applications),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => _showStopMethodDialog(context, settings),
                );
              },
            ),
          ]),
          const SizedBox(height: 20),
          _buildSection(context, 'Advanced', [
            Consumer<SettingsProvider>(
              builder: (context, settings, _) {
                return SwitchListTile(
                  title: const Text('No Sleep Mode'),
                  subtitle: const Text(
                    'Prevent timer extension (parental control)',
                  ),
                  value: settings.noSleepMode,
                  onChanged: (_) => settings.toggleNoSleepMode(),
                  secondary: const Icon(Icons.lock_clock),
                );
              },
            ),
            ListTile(
              title: const Text('Permissions'),
              subtitle: const Text('Manage app permissions'),
              leading: const Icon(Icons.security),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () => PermissionService.openSettings(),
            ),
          ]),
          const SizedBox(height: 20),
          _buildSection(context, 'About', [
            const ListTile(
              title: Text('Version'),
              subtitle: Text('1.0.0'),
              leading: Icon(Icons.info),
            ),
            const ListTile(
              title: Text('Developer'),
              subtitle: Text('Smart Sleep Timer Team'),
              leading: Icon(Icons.code),
            ),
          ]),
        ],
      ),
    );
  }

  Widget _buildSection(
    BuildContext context,
    String title,
    List<Widget> children,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 16, bottom: 8),
          child: Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        Card(child: Column(children: children)),
      ],
    );
  }

  String _getStopMethodLabel(String method) {
    switch (method) {
      case 'force_close':
        return 'Force close apps';
      case 'mute':
        return 'Mute device';
      case 'pause':
        return 'Pause media only';
      default:
        return 'Unknown';
    }
  }

  void _showStopMethodDialog(BuildContext context, SettingsProvider settings) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Select Stop Method'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            RadioListTile<String>(
              title: const Text('Force Close Apps'),
              subtitle: const Text('Close media apps completely'),
              value: 'force_close',
              groupValue: settings.stopMethod,
              onChanged: (value) {
                if (value != null) {
                  settings.setStopMethod(value);
                  Navigator.pop(context);
                }
              },
            ),
            RadioListTile<String>(
              title: const Text('Mute Device'),
              subtitle: const Text('Mute all sounds'),
              value: 'mute',
              groupValue: settings.stopMethod,
              onChanged: (value) {
                if (value != null) {
                  settings.setStopMethod(value);
                  Navigator.pop(context);
                }
              },
            ),
            RadioListTile<String>(
              title: const Text('Pause Media'),
              subtitle: const Text('Pause playback only'),
              value: 'pause',
              groupValue: settings.stopMethod,
              onChanged: (value) {
                if (value != null) {
                  settings.setStopMethod(value);
                  Navigator.pop(context);
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
