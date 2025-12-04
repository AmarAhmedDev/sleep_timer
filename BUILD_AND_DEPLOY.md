# Build and Deploy Guide - Smart Sleep Timer

## 🚀 Complete Build & Deployment Guide

This guide covers everything from development to production deployment.

---

## 📋 Prerequisites Checklist

### Required Software
- [ ] Flutter SDK 3.9.2 or higher
- [ ] Dart SDK (comes with Flutter)
- [ ] Android Studio or VS Code
- [ ] Android SDK (API 24-34)
- [ ] Java JDK 11 or higher
- [ ] Git (optional, for version control)

### Verify Installation
```bash
flutter doctor -v
```

All items should show ✓ (checkmarks). Fix any issues before proceeding.

---

## 🔧 Development Setup

### 1. Clone/Download Project
```bash
cd sleep_timer
```

### 2. Install Dependencies
```bash
flutter pub get
```

Expected output: "Got dependencies!"

### 3. Verify Project
```bash
flutter analyze
```

Should show no errors.

### 4. Connect Device
**Physical Device:**
```bash
adb devices
```

**Emulator:**
- Open Android Studio → AVD Manager → Start emulator

### 5. Run in Debug Mode
```bash
flutter run
```

App should launch on your device/emulator.

---

## 🏗️ Building Release APK

### Method 1: Command Line (Recommended)

#### Step 1: Clean Previous Builds
```bash
flutter clean
```

#### Step 2: Get Dependencies
```bash
flutter pub get
```

#### Step 3: Build Release APK
```bash
flutter build apk --release
```

#### Step 4: Locate APK
```
build/app/outputs/flutter-apk/app-release.apk
```

**File Size:** Approximately 20-30 MB

---

### Method 2: Windows Batch Script

Simply double-click: `build_apk.bat`

The script will:
1. Clean previous builds
2. Get dependencies
3. Build release APK
4. Show output location

---

### Method 3: Split APKs (Smaller Size)

Build separate APKs for different architectures:

```bash
flutter build apk --split-per-abi
```

This creates:
- `app-armeabi-v7a-release.apk` (~15 MB) - 32-bit ARM
- `app-arm64-v8a-release.apk` (~18 MB) - 64-bit ARM
- `app-x86_64-release.apk` (~20 MB) - 64-bit x86

**Benefit:** Smaller download size for users

---

### Method 4: App Bundle (For Play Store)

```bash
flutter build appbundle --release
```

Output: `build/app/outputs/bundle/release/app-release.aab`

**Use this for Google Play Store submission.**

---

## 🔐 Code Signing (For Production)

### Generate Keystore

```bash
keytool -genkey -v -keystore ~/upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

### Configure Signing

Create `android/key.properties`:
```properties
storePassword=<your-store-password>
keyPassword=<your-key-password>
keyAlias=upload
storeFile=<path-to-keystore>/upload-keystore.jks
```

### Update build.gradle.kts

Add to `android/app/build.gradle.kts`:

```kotlin
// Load keystore
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing config ...
    
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Build Signed APK
```bash
flutter build apk --release
```

---

## 📦 Distribution Methods

### 1. Direct APK Distribution

**Pros:**
- No store approval needed
- Instant distribution
- Full control

**Cons:**
- Users must enable "Unknown sources"
- No automatic updates
- Manual distribution

**Steps:**
1. Build release APK
2. Upload to website/cloud storage
3. Share download link
4. Users install manually

**Installation Instructions for Users:**
```
1. Download APK file
2. Settings → Security → Enable "Install unknown apps"
3. Open APK file
4. Tap "Install"
5. Grant permissions when prompted
```

---

### 2. Google Play Store

**Pros:**
- Trusted source
- Automatic updates
- Wide reach
- Better security

**Cons:**
- Review process (1-7 days)
- Developer fee ($25 one-time)
- Store policies

**Steps:**

#### A. Prepare Assets

**App Icon:**
- 512x512 PNG
- No transparency
- Follows Material Design

**Screenshots:**
- At least 2 screenshots
- 16:9 or 9:16 ratio
- Show key features

**Feature Graphic:**
- 1024x500 PNG
- Promotional banner

#### B. Create Play Console Account
1. Go to https://play.google.com/console
2. Pay $25 registration fee
3. Complete account setup

#### C. Create App Listing
1. Click "Create app"
2. Fill in app details:
   - App name: Smart Sleep Timer
   - Default language: English
   - App type: App
   - Free/Paid: Free

#### D. Complete Store Listing
- Short description (80 chars)
- Full description (4000 chars)
- Screenshots
- Feature graphic
- App icon
- Category: Tools
- Content rating: Everyone

#### E. Upload App Bundle
```bash
flutter build appbundle --release
```

1. Go to "Release" → "Production"
2. Click "Create new release"
3. Upload `app-release.aab`
4. Add release notes
5. Review and rollout

#### F. Submit for Review
- Complete all required sections
- Submit for review
- Wait for approval (1-7 days)

---

### 3. Alternative App Stores

**Amazon Appstore:**
- Similar to Play Store
- Good for Fire devices

**Samsung Galaxy Store:**
- Pre-installed on Samsung devices
- Good reach in some regions

**APKPure, APKMirror:**
- Third-party stores
- No developer account needed

---

## 🧪 Pre-Release Testing

### 1. Internal Testing

Test on multiple devices:
- [ ] Android 9 device
- [ ] Android 10 device
- [ ] Android 11+ device
- [ ] Different manufacturers (Samsung, Google, Xiaomi)

### 2. Feature Testing

- [ ] All timer presets work
- [ ] Custom timer works
- [ ] Pause/Resume works
- [ ] Stop works
- [ ] Background service runs
- [ ] Notifications appear
- [ ] Media apps close
- [ ] Settings save
- [ ] History saves
- [ ] Dark mode works
- [ ] Permissions request properly

### 3. Edge Cases

- [ ] Timer with 0 minutes (should reject)
- [ ] Very long timer (24 hours)
- [ ] Multiple rapid starts/stops
- [ ] Low battery scenario
- [ ] Low storage scenario
- [ ] Airplane mode
- [ ] No media apps installed

### 4. Performance Testing

- [ ] App starts quickly (<2 seconds)
- [ ] No memory leaks
- [ ] Battery usage acceptable
- [ ] No crashes
- [ ] Smooth animations

---

## 📊 Build Optimization

### Reduce APK Size

```bash
flutter build apk --release --shrink --split-per-abi
```

Flags:
- `--shrink`: Remove unused code
- `--split-per-abi`: Separate APKs per architecture

### Enable ProGuard

Add to `android/app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### Optimize Images

- Use WebP format
- Compress images
- Remove unused assets

---

## 🔍 Quality Assurance

### Pre-Deployment Checklist

#### Code Quality
- [ ] No compiler errors
- [ ] No analyzer warnings
- [ ] Code formatted (`flutter format .`)
- [ ] All imports used
- [ ] No debug prints in production

#### Functionality
- [ ] All features work
- [ ] No crashes
- [ ] Permissions work
- [ ] Background service works
- [ ] Notifications work

#### UI/UX
- [ ] No UI glitches
- [ ] Animations smooth
- [ ] Text readable
- [ ] Icons display correctly
- [ ] Dark mode works

#### Performance
- [ ] App starts quickly
- [ ] No lag
- [ ] Battery usage acceptable
- [ ] Memory usage normal

#### Security
- [ ] No hardcoded secrets
- [ ] Permissions justified
- [ ] Secure data storage
- [ ] No sensitive logs

#### Documentation
- [ ] README complete
- [ ] Setup instructions clear
- [ ] Troubleshooting guide available
- [ ] Code commented

---

## 📱 Post-Deployment

### Monitor Issues

1. **Crash Reports**
   - Set up Firebase Crashlytics (optional)
   - Monitor Play Console crash reports

2. **User Feedback**
   - Read reviews
   - Respond to issues
   - Track feature requests

3. **Analytics** (Optional)
   - Track usage patterns
   - Identify popular features
   - Find pain points

### Update Strategy

**Version Numbering:**
- Major.Minor.Patch (e.g., 1.0.0)
- Major: Breaking changes
- Minor: New features
- Patch: Bug fixes

**Update pubspec.yaml:**
```yaml
version: 1.0.1+2
```
- 1.0.1 = Version name
- 2 = Build number

**Release Notes Template:**
```
Version 1.0.1
- Fixed: Timer not stopping on some devices
- Improved: Battery optimization
- Added: New timer preset (45 minutes)
```

---

## 🚨 Troubleshooting Build Issues

### Issue: "Flutter command not found"
```bash
# Add Flutter to PATH
export PATH="$PATH:/path/to/flutter/bin"
```

### Issue: "Android SDK not found"
```bash
flutter doctor --android-licenses
```

### Issue: "Gradle build failed"
```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
flutter build apk
```

### Issue: "Out of memory"
Add to `android/gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Issue: "Build takes too long"
```bash
flutter build apk --release --no-tree-shake-icons
```

---

## 📈 Success Metrics

### Key Performance Indicators

- **Install Rate**: Track downloads
- **Retention**: Users returning after 1 day, 7 days, 30 days
- **Crash-Free Rate**: Target >99%
- **Rating**: Target >4.0 stars
- **Reviews**: Monitor feedback

---

## 🎯 Launch Checklist

### Pre-Launch
- [ ] All features tested
- [ ] No critical bugs
- [ ] Documentation complete
- [ ] Screenshots ready
- [ ] Store listing prepared
- [ ] Privacy policy (if needed)
- [ ] Terms of service (if needed)

### Launch Day
- [ ] Upload to store
- [ ] Submit for review
- [ ] Announce on social media
- [ ] Share with beta testers
- [ ] Monitor for issues

### Post-Launch
- [ ] Respond to reviews
- [ ] Fix critical bugs quickly
- [ ] Plan next update
- [ ] Gather feedback
- [ ] Track metrics

---

## 🎉 Congratulations!

You're now ready to build and deploy Smart Sleep Timer!

### Quick Commands Reference

```bash
# Development
flutter run

# Build APK
flutter build apk --release

# Build App Bundle
flutter build appbundle --release

# Build Split APKs
flutter build apk --split-per-abi

# Clean
flutter clean

# Analyze
flutter analyze

# Format
flutter format .
```

---

**Good luck with your deployment! 🚀**
