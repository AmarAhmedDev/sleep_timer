# Troubleshooting Guide - Smart Sleep Timer

## 🔧 Common Issues and Solutions

### 1. Timer Doesn't Stop Media Apps

#### Symptoms
- Timer expires but media keeps playing
- Apps don't close automatically

#### Solutions

**A. Check Usage Stats Permission**
```
Settings → Apps → Special app access → Usage access → Smart Sleep Timer → Enable
```
This is the MOST IMPORTANT permission for detecting and closing apps.

**B. Disable Battery Optimization**
```
Settings → Apps → Smart Sleep Timer → Battery → Unrestricted
```

**C. Check Stop Method**
- Open Settings in the app
- Try different stop methods:
  - Force Close (most aggressive)
  - Mute (less intrusive)
  - Pause (least intrusive)

**D. Manufacturer-Specific Settings**

**Samsung:**
```
Settings → Apps → Smart Sleep Timer → Battery → Allow background activity
Settings → Device care → Battery → App power management → Apps that won't be put to sleep → Add Smart Sleep Timer
```

**Xiaomi/MIUI:**
```
Settings → Apps → Manage apps → Smart Sleep Timer → Autostart → Enable
Settings → Apps → Manage apps → Smart Sleep Timer → Battery saver → No restrictions
```

**Huawei:**
```
Settings → Apps → Apps → Smart Sleep Timer → Battery → App launch → Manage manually
Enable: Auto-launch, Secondary launch, Run in background
```

**OnePlus/Oppo:**
```
Settings → Apps → Smart Sleep Timer → Battery → Battery optimization → Don't optimize
```

---

### 2. Timer Stops in Background

#### Symptoms
- Timer pauses when app is closed
- Notification disappears
- Timer doesn't complete

#### Solutions

**A. Keep App in Recent Apps**
- Don't swipe away the app from recent apps
- Let it run in background

**B. Disable Battery Saver**
```
Settings → Battery → Battery Saver → Turn OFF
```

**C. Lock App in Recent Apps**
- Open recent apps
- Find Smart Sleep Timer
- Tap the lock icon (if available)

**D. Check Foreground Service**
- You should see a persistent notification
- If notification disappears, the service stopped
- Restart the app and check permissions

---

### 3. No Notifications Appearing

#### Symptoms
- No notification when timer starts
- No warning at 1 minute
- No completion notification

#### Solutions

**A. Check Notification Permission**
```
Settings → Apps → Smart Sleep Timer → Notifications → Enable
```

**B. Check Do Not Disturb**
```
Settings → Sound → Do Not Disturb → Turn OFF
or
Allow Smart Sleep Timer in DND exceptions
```

**C. Check Notification Channels**
```
Settings → Apps → Smart Sleep Timer → Notifications → 
Enable all notification categories
```

**D. Reset App Notifications**
```
Settings → Apps → Smart Sleep Timer → Notifications → 
Reset to default
```

---

### 4. App Crashes on Startup

#### Symptoms
- App closes immediately after opening
- Black screen then crash
- "App has stopped" error

#### Solutions

**A. Clear App Data**
```
Settings → Apps → Smart Sleep Timer → Storage → Clear data
```
Note: This will reset all settings and history.

**B. Reinstall the App**
1. Uninstall Smart Sleep Timer
2. Restart device
3. Install again
4. Grant all permissions

**C. Check Android Version**
- App requires Android 9 or higher
- Check: Settings → About phone → Android version

**D. Check Available Storage**
- Ensure at least 100MB free space
- Settings → Storage

---

### 5. Vibration Not Working

#### Symptoms
- No vibration when timer ends
- Vibration setting enabled but not working

#### Solutions

**A. Check Device Vibration**
```
Settings → Sound → Vibration → Enable
```

**B. Check App Setting**
- Open Smart Sleep Timer
- Go to Settings tab
- Ensure "Vibration" is enabled

**C. Check Do Not Disturb**
- DND mode may block vibrations
- Disable DND or add exception

---

### 6. Screen Doesn't Lock/Go Home

#### Symptoms
- Timer ends but stays on current app
- Screen doesn't turn off

#### Solutions

**A. Check Screen Off Setting**
- Open Smart Sleep Timer Settings
- Enable "Screen Off" option

**B. Check Overlay Permission**
```
Settings → Apps → Special app access → Display over other apps → Smart Sleep Timer → Enable
```

**C. Android 10+ Limitation**
- Android 10+ restricts screen locking
- App will redirect to home screen instead

---

### 7. Specific Apps Not Closing

#### Symptoms
- YouTube closes but TikTok doesn't
- Some apps keep running

#### Solutions

**A. Check App Package Name**
The app targets these packages:
- YouTube: com.google.android.youtube
- TikTok: com.zhiliaoapp.musically
- Facebook: com.facebook.katana
- Instagram: com.instagram.android
- Telegram: org.telegram.messenger
- Spotify: com.spotify.music

**B. Try Different Stop Method**
- Force Close → Mute → Pause
- Some apps respond better to different methods

**C. Android 10+ Limitation**
- Android 10+ restricts killing other apps
- Best effort approach used
- Root access would provide better control (not required)

---

### 8. Custom Timer Not Working

#### Symptoms
- Can't input custom time
- Custom timer button doesn't respond

#### Solutions

**A. Check No Sleep Mode**
- If No Sleep Mode is enabled and timer is running
- You can't start a new timer
- Stop current timer first

**B. Input Valid Time**
- Enter at least 1 minute
- Maximum recommended: 24 hours

**C. Check Keyboard**
- Ensure numeric keyboard appears
- Try tapping the input field again

---

### 9. History Not Saving

#### Symptoms
- History screen is empty
- Completed timers don't appear

#### Solutions

**A. Check Storage Permission**
- App should have storage access
- Check: Settings → Apps → Smart Sleep Timer → Permissions

**B. Complete Timer Fully**
- History only saves when timer reaches 00:00
- Stopped timers are not saved

**C. Clear App Cache**
```
Settings → Apps → Smart Sleep Timer → Storage → Clear cache
```
Note: This won't delete history, only cache.

---

### 10. Dark Mode Not Working

#### Symptoms
- Dark mode toggle doesn't work
- App stays in light/dark mode

#### Solutions

**A. Check System Theme**
- App respects system theme by default
- Change in app settings to override

**B. Restart App**
- Close app completely
- Reopen to apply theme

**C. Clear App Data**
- Last resort: Clear data and reconfigure

---

## 🔍 Diagnostic Steps

### Step 1: Check Permissions
Run through all permissions:
1. Notifications ✓
2. Usage Stats ✓
3. Display Over Other Apps ✓
4. Battery Optimization Exception ✓

### Step 2: Test Basic Timer
1. Set 1-minute timer
2. Wait for completion
3. Check if notification appears

### Step 3: Test Media Control
1. Open YouTube
2. Play a video
3. Set 1-minute timer
4. Wait and observe

### Step 4: Check Logs
If you're a developer:
```bash
adb logcat | grep -i "sleep"
```

---

## 📱 Manufacturer-Specific Issues

### Samsung Devices
- **Issue**: Aggressive battery optimization
- **Solution**: Add to "Never sleeping apps" list

### Xiaomi/MIUI Devices
- **Issue**: Kills background apps aggressively
- **Solution**: Enable Autostart and disable battery restrictions

### Huawei Devices
- **Issue**: Protected apps system
- **Solution**: Add to protected apps list

### OnePlus Devices
- **Issue**: Battery optimization
- **Solution**: Disable battery optimization

### Oppo/Realme Devices
- **Issue**: Background app restrictions
- **Solution**: Enable background activity

---

## 🆘 Still Having Issues?

### Before Reporting
1. ✓ Checked all permissions
2. ✓ Disabled battery optimization
3. ✓ Tested with different apps
4. ✓ Tried different stop methods
5. ✓ Restarted device
6. ✓ Reinstalled app

### Information to Provide
- Android version
- Device manufacturer and model
- Specific app that won't close
- Stop method being used
- Error messages (if any)
- Steps to reproduce

---

## 💡 Pro Tips

1. **Best Permissions Setup**
   - Grant ALL permissions when prompted
   - Don't skip any permission requests

2. **Optimal Settings**
   - Stop Method: Force Close
   - Vibration: Enabled
   - Screen Off: Enabled
   - Battery Optimization: Disabled

3. **For Parents**
   - Enable "No Sleep Mode"
   - Use Force Close method
   - Set device PIN to prevent app uninstall

4. **Battery Life**
   - Timer uses minimal battery
   - Foreground service is optimized
   - Only runs when timer is active

5. **Testing**
   - Test with short timers first (1-2 min)
   - Verify notifications work
   - Then use longer timers

---

## 🔄 Reset to Default

If all else fails, reset the app:

1. **Clear App Data**
   ```
   Settings → Apps → Smart Sleep Timer → Storage → Clear data
   ```

2. **Uninstall and Reinstall**
   - Uninstall app
   - Restart device
   - Install fresh copy
   - Grant all permissions

3. **Factory Reset** (Last Resort)
   - Backup your device
   - Factory reset
   - Reinstall app

---

## ✅ Verification Checklist

After fixing issues, verify:
- [ ] Timer starts correctly
- [ ] Notification appears
- [ ] Timer runs in background
- [ ] 1-minute warning shows
- [ ] Media stops at 00:00
- [ ] Completion notification appears
- [ ] History saves
- [ ] Settings persist

---

**Most issues are permission-related. Always check permissions first!** 🔑
