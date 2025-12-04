@echo off
echo Building Smart Sleep Timer APK...
echo.

echo Step 1: Cleaning previous builds...
call flutter clean

echo.
echo Step 2: Getting dependencies...
call flutter pub get

echo.
echo Step 3: Building release APK...
call flutter build apk --release

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo.
echo APK Location: build\app\outputs\flutter-apk\app-release.apk
echo.
pause
