import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';
import 'screens/home_screen.dart';
import 'screens/history_screen.dart';
import 'screens/settings_screen.dart';
import 'providers/timer_provider.dart';
import 'providers/settings_provider.dart';
import 'services/notification_service.dart';
import 'services/permission_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize services
  await NotificationService.initialize();
  FlutterForegroundTask.init(
    androidNotificationOptions: AndroidNotificationOptions(
      channelId: 'sleep_timer_channel',
      channelName: 'Sleep Timer',
      channelDescription: 'Sleep timer running in background',
      channelImportance: NotificationChannelImportance.LOW,
      priority: NotificationPriority.LOW,
    ),
    iosNotificationOptions: const IOSNotificationOptions(),
    foregroundTaskOptions: ForegroundTaskOptions(
      eventAction: ForegroundTaskEventAction.repeat(1000),
      autoRunOnBoot: false,
      allowWakeLock: true,
      allowWifiLock: false,
    ),
  );

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);

    // Set system UI overlay style for cosmic theme
    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light,
        systemNavigationBarColor: Color(0xFF0f0f23),
        systemNavigationBarIconBrightness: Brightness.light,
      ),
    );

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => TimerProvider()),
        ChangeNotifierProvider(create: (_) => SettingsProvider()),
      ],
      child: MaterialApp(
        title: 'Smart Sleep Timer',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          brightness: Brightness.dark,
          scaffoldBackgroundColor: const Color(0xFF0f0f23),
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF6366f1),
            brightness: Brightness.dark,
            surface: const Color(0xFF1a1a2e),
            onSurface: Colors.white,
          ),
          navigationBarTheme: NavigationBarThemeData(
            backgroundColor: const Color(0xFF1a1a2e).withOpacity(0.9),
            indicatorColor: const Color(0xFF6366f1).withOpacity(0.3),
            labelTextStyle: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.selected)) {
                return const TextStyle(
                  color: Color(0xFF6366f1),
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                );
              }
              return TextStyle(
                color: Colors.white.withOpacity(0.5),
                fontSize: 12,
              );
            }),
            iconTheme: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.selected)) {
                return const IconThemeData(color: Color(0xFF6366f1), size: 26);
              }
              return IconThemeData(
                color: Colors.white.withOpacity(0.5),
                size: 24,
              );
            }),
          ),
        ),
        home: const MainScreen(),
      ),
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _currentIndex = 0;

  final List<Widget> _screens = [
    const HomeScreen(),
    const HistoryScreen(),
    const SettingsScreen(),
  ];

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    await PermissionService.requestAllPermissions(context);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBody: true,
      body: _screens[_currentIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Colors.transparent,
              const Color(0xFF0f0f23).withOpacity(0.8),
            ],
          ),
        ),
        child: NavigationBar(
          selectedIndex: _currentIndex,
          onDestinationSelected: (index) {
            setState(() {
              _currentIndex = index;
            });
          },
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.nightlight_outlined),
              selectedIcon: Icon(Icons.nightlight_round),
              label: 'Sleep',
            ),
            NavigationDestination(
              icon: Icon(Icons.auto_graph_outlined),
              selectedIcon: Icon(Icons.auto_graph),
              label: 'History',
            ),
            NavigationDestination(
              icon: Icon(Icons.tune_outlined),
              selectedIcon: Icon(Icons.tune),
              label: 'Settings',
            ),
          ],
        ),
      ),
    );
  }
}
