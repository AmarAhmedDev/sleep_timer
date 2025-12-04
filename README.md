# Smart Sleep Timer 🌙

A Flutter mobile application that helps users avoid sleeping while videos or audio keep playing in the background. The app automatically closes/force stops or mutes any opened application when the timer ends.

## 🎯 Features

### ⏱️ Smart Sleep Timer
- Timer presets: 5, 10, 20, 30, 60 minutes
- Custom manual timer input
- Vibrating alert when timer ends
- Sound alert notifications
- Beautiful circular timer animation

### 🎵 Auto Stop Running Media Apps
When countdown reaches zero:
- Stop music playback
- Pause video
- Force close media apps
- Redirect to home screen

**Supported Apps:**
- YouTube
- TikTok
- Facebook
- Instagram
- Telegram
- Spotify
- MX Player
- Local Music Players

### 📱 Background Tracking
- Timer runs in background even when app is closed
- Detects if video/audio is still playing
- Monitors sound output usage
- Foreground service keeps timer active

### 🔒 Required Permissions
- Overlay screen permission
- Usage access permission
- Battery optimization exception
- Notification permission

### 📊 Features
- **History Screen**: Track previous timers
- **Settings Screen**: 
  - Auto-stop method selector (Force Close / Mute / Pause)
  - Enable/disable vibration
  - Enable/disable screen-off when finished
  - Dark mode support
  - No Sleep Mode (parental control)

### 💡 No Sleep Mode
Optional feature for parental control:
- When enabled, user cannot extend timer
- Forces shutdown when timer ends
- Perfect for parents controlling kids' screen time

## 🚀 Getting Started

### Prerequisites
- Flutter SDK (3.9.2 or higher)
- Android Studio / VS Code
- Android device or emulator (Android 9+)

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd sleep_timer
```

2. Install dependencies
```bash
flutter pub get
```

3. Run the app
```bash
flutter run
```

### Build Release APK
```bash
flutter build apk --release
```

The APK will be available at: `build/app/outputs/flutter-apk/app-release.apk`

## 📱 Platform Support
- ✅ Android 9 to Android 15
- ❌ iOS (not supported - requires different approach)

## 🛠️ Technical Stack
- **Framework**: Flutter + Dart
- **State Management**: Provider
- **Background Service**: flutter_foreground_task
- **Notifications**: flutter_local_notifications
- **Permissions**: permission_handler
- **Storage**: shared_preferences
- **Native Integration**: MethodChannel for Android media control

## 🔧 Architecture
```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
│   └── timer_history.dart
├── providers/                # State management
│   ├── timer_provider.dart
│   └── settings_provider.dart
├── screens/                  # UI screens
│   ├── home_screen.dart
│   ├── history_screen.dart
│   └── settings_screen.dart
├── services/                 # Business logic
│   ├── media_control_service.dart
│   ├── notification_service.dart
│   └── permission_service.dart
└── widgets/                  # Reusable widgets
    ├── timer_display.dart
    ├── timer_presets.dart
    └── custom_timer_dialog.dart
```

## 📝 How It Works

1. **User sets timer**: Choose from presets or set custom time
2. **Timer runs in background**: Foreground service keeps it active
3. **1 minute warning**: Notification alerts user
4. **Timer expires**:
   - Detects foreground app
   - If it's a media app, force closes it
   - Mutes device audio
   - Shows completion notification
   - Redirects to home screen

## ⚠️ Important Notes

- **Usage Stats Permission**: Required to detect foreground apps. Users must manually enable this in Android settings.
- **Battery Optimization**: App requests exemption to run reliably in background.
- **Force Stop Limitation**: Android 10+ restricts force-stopping apps. The app uses best-effort approach.

## 🎨 UI Design
- Modern gradient background
- Sleep theme with moon and stars icons
- Rounded timer buttons
- Circular timer animation
- Full dark mode support

## 📄 License
This project is licensed under the MIT License.

## 👨‍💻 Developer
Smart Sleep Timer Team

## 🤝 Contributing
Contributions, issues, and feature requests are welcome!
