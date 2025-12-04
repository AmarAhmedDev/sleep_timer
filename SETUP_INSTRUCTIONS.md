# Smart Sleep Timer - Setup Instructions

## 📋 Prerequisites

Before building the app, ensure you have:

1. **Flutter SDK** installed (version 3.9.2 or higher)
   - Download from: https://flutter.dev/docs/get-started/install
   - Add Flutter to your PATH

2. **Android Studio** or **VS Code** with Flutter extensions

3. **Android SDK** (API level 24 or higher)
   - Android Studio will install this automatically
   - Or use `flutter doctor` to check requirements

## 🔧 Setup Steps

### 1. Install Dependencies

Open terminal in the project directory and run:

```bash
flutter pub get
```

### 2. Check Flutter Setup

```bash
flutter doctor
```

Fix any issues reported by Flutter Doctor.

### 3. Connect Device or Start Emulator

**For Physical Device:**
- Enable Developer Options on your Android device
- Enable USB Debugging
- Connect via USB

**For Emulator:**
- Open Android Studio
- Go to AVD Manager
- Create/Start an Android Virtual Device

### 4. Run the App

```bash
flutter run
```

## 📦 Building Release APK

### Option 1: Using Command Line

```bash
flutter build apk --release
```

The APK will be at: `build/app/outputs/flutter-apk/app-release.apk`

### Option 2: Using Build Script (Windows)

Double-click `build_apk.bat` or run:

```bash
build_apk.bat
```

### Option 3: Build App Bundle (for Play Store)

```bash
flutter build appbundle --release
```

## 🔑 Required Permissions Setup

After installing the app, you need to grant special permissions:

### 1. Usage Access Permission
1. Open the app
2. When prompted, tap "Grant Permission"
3. Find "Smart Sleep Timer" in the list
4. Toggle "Permit usage access" ON

**Manual Path:**
Settings → Apps → Special app access → Usage access → Smart Sleep Timer → Enable

### 2. Display Over Other Apps
1. When prompted, tap "Grant Permission"
2. Toggle "Allow display over other apps" ON

**Manual Path:**
Settings → Apps → Special app access → Display over other apps → Smart Sleep Timer → Enable

### 3. Battery Optimization
1. When prompted, tap "Grant Permission"
2. Select "Don't optimize" or "Allow"

**Manual Path:**
Settings → Apps → Smart Sleep Timer → Battery → Unrestricted

### 4. Notifications
Should be granted automatically, but if not:

**Manual Path:**
Settings → Apps → Smart Sleep Timer → Notifications → Enable

## 🧪 Testing the App

### Test Timer Functionality:
1. Open the app
2. Select a timer preset (e.g., "5 min")
3. Timer should start counting down
4. App should show notification

### Test Media Control:
1. Open YouTube or Spotify
2. Start playing a video/song
3. Return to Smart Sleep Timer
4. Set a short timer (1-2 minutes)
5. Wait for timer to expire
6. Media should stop and app should close

### Test Background Service:
1. Start a timer
2. Press home button or switch to another app
3. Timer should continue running in background
4. Check notification bar for timer status

## 🐛 Troubleshooting

### Issue: "flutter: command not found"
**Solution:** Add Flutter to your system PATH
- Windows: Add `C:\path\to\flutter\bin` to PATH
- Mac/Linux: Add `export PATH="$PATH:/path/to/flutter/bin"` to `.bashrc` or `.zshrc`

### Issue: "Android SDK not found"
**Solution:** 
1. Install Android Studio
2. Open Android Studio → SDK Manager
3. Install Android SDK (API 24+)
4. Run `flutter doctor --android-licenses`

### Issue: App doesn't stop media
**Solution:**
1. Ensure Usage Access permission is granted
2. Check that battery optimization is disabled
3. Some apps (like YouTube) may require force stop which needs root on Android 10+

### Issue: Timer doesn't run in background
**Solution:**
1. Grant battery optimization exception
2. Disable battery saver mode
3. Check if app is not restricted in background

### Issue: No notifications
**Solution:**
1. Grant notification permission
2. Check notification settings for the app
3. Ensure "Do Not Disturb" is off

## 📱 Supported Android Versions

- ✅ Android 9 (API 28)
- ✅ Android 10 (API 29)
- ✅ Android 11 (API 30)
- ✅ Android 12 (API 31)
- ✅ Android 13 (API 33)
- ✅ Android 14 (API 34)
- ✅ Android 15 (API 35)

## 🔐 Security Notes

- The app requires sensitive permissions (Usage Stats, Overlay)
- These are necessary for core functionality
- The app does NOT collect or transmit any user data
- All data is stored locally on device

## 📞 Support

If you encounter issues:
1. Check this guide first
2. Run `flutter doctor -v` and check for issues
3. Check app permissions in Android settings
4. Try reinstalling the app

## 🎯 Next Steps

After successful setup:
1. Customize timer presets in Settings
2. Enable/disable features as needed
3. Test with your favorite media apps
4. Share with friends and family!

---

**Happy Sleeping! 😴🌙**
