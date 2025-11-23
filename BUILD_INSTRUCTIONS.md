# Build Instructions for MiniCount

Complete guide to building MiniCount for development and release.

## Prerequisites

### Required Software
1. **Android Studio** - Hedgehog (2023.1.1) or newer
   - Download: https://developer.android.com/studio

2. **Java Development Kit (JDK) 17**
   - Android Studio includes JDK
   - Or download: https://adoptium.net/

3. **Android SDK**
   - API Level 26 (Android 8.0) - Minimum
   - API Level 34 (Android 14) - Target & Compile
   - Install via Android Studio SDK Manager

4. **Git** (for version control)
   - Download: https://git-scm.com/

### Optional Tools
- **Gradle** (included with project)
- **Android Emulator** or physical device for testing

## Development Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd MiniCount
```

### 2. Open in Android Studio
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to MiniCount directory
4. Click "OK"

### 3. Sync Gradle
- Android Studio will automatically prompt to sync
- Or manually: File → Sync Project with Gradle Files
- Wait for sync to complete (may take a few minutes first time)

### 4. Configure SDK
If prompted:
1. Open SDK Manager (Tools → SDK Manager)
2. Install:
   - Android SDK Platform 34
   - Android SDK Build-Tools
   - Android Emulator (optional)

### 5. Run on Emulator

#### Create AVD (Android Virtual Device):
1. Tools → Device Manager
2. Create Device
3. Select hardware (e.g., Pixel 6)
4. Select system image (API 34 recommended)
5. Finish and start emulator

#### Run app:
1. Select device/emulator in toolbar
2. Click Run (green play button) or Shift+F10
3. App will build and launch

### 6. Run on Physical Device

#### Enable Developer Mode:
1. Settings → About Phone
2. Tap "Build Number" 7 times
3. Settings → System → Developer Options
4. Enable "USB Debugging"

#### Connect and Run:
1. Connect device via USB
2. Authorize USB debugging on device
3. Select device in Android Studio
4. Click Run

## Build Variants

### Debug Build
For development and testing:
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

Features:
- Debug symbols included
- Logging enabled
- No code optimization
- Not signed for distribution

### Release Build
For Play Store submission:

#### 1. Create Keystore (First Time Only)
```bash
keytool -genkey -v -keystore minicount.keystore \
  -alias minicount \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Follow prompts to set:
- Password for keystore
- Name, organization, location
- Password for key

**IMPORTANT**: Save passwords securely! You'll need them for all future releases.

#### 2. Create keystore.properties
Create file in project root:
```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=minicount
storeFile=../minicount.keystore
```

**NEVER commit this file to git!** (already in .gitignore)

#### 3. Build Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

#### 4. Build Release AAB (for Play Store)
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

**Use AAB for Play Store submission** (smaller download size for users)

## Configuration for Release

### 1. Update AdMob IDs
Replace test IDs with your real AdMob IDs:

**File**: `app/src/main/AndroidManifest.xml`
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" />
```

**File**: `app/src/main/java/com/minicount/app/presentation/components/AdBanner.kt`
```kotlin
adUnitId = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
```

### 2. Configure Google Play Billing

**File**: `app/src/main/java/com/minicount/app/domain/billing/BillingManager.kt`

Update product ID to match your Play Console configuration:
```kotlin
const val PREMIUM_SKU = "premium_unlock" // Must match Play Console
```

### 3. Update Version
**File**: `app/build.gradle.kts`
```kotlin
versionCode = 1  // Increment for each release
versionName = "1.0.0"  // Semantic version
```

### 4. Verify Package Name
Ensure package name is unique (default is fine):
```kotlin
applicationId = "com.minicount.app"
```

## Testing Before Release

### Run All Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Create event
- [ ] Edit event
- [ ] Delete event
- [ ] Add widget to home screen
- [ ] Widget displays correctly
- [ ] Widget updates countdown
- [ ] Photo selection works
- [ ] Notifications appear
- [ ] Premium purchase flow
- [ ] Ads display (free version)
- [ ] No ads (premium version)
- [ ] App doesn't crash
- [ ] All screens render correctly
- [ ] Dark mode works
- [ ] Landscape orientation works

### Performance Testing
```bash
./gradlew app:assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

Check:
- App size (should be under 10MB)
- Cold start time (should be under 2 seconds)
- Memory usage (should be under 100MB)
- Battery drain (should be minimal)

## Troubleshooting

### Gradle Sync Failed
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

### Build Failed - Dependency Issues
1. Check internet connection
2. Invalidate caches: File → Invalidate Caches → Invalidate and Restart
3. Delete `.gradle` folder and sync again

### Widget Not Updating
1. Force stop app
2. Clear app data
3. Re-add widget

### Signing Failed
- Verify keystore.properties exists and has correct values
- Ensure keystore file path is correct
- Check passwords are correct

### Out of Memory During Build
Add to `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Build Scripts

Create `build.sh` for convenience:
```bash
#!/bin/bash
./gradlew clean
./gradlew bundleRelease
echo "Build complete: app/build/outputs/bundle/release/app-release.aab"
```

Make executable:
```bash
chmod +x build.sh
./build.sh
```

## CI/CD Setup (Optional)

For automated builds on GitHub Actions, GitLab CI, etc.

### GitHub Actions Example
Create `.github/workflows/android.yml`:
```yaml
name: Android Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew assembleDebug

    - name: Run tests
      run: ./gradlew test
```

## Play Store Submission

After successful build:

1. **Upload AAB** to Play Console
2. **Fill Store Listing** (see PLAY_STORE_LISTING.md)
3. **Add Screenshots** (minimum 2 for phone)
4. **Set Content Rating** (via questionnaire)
5. **Complete Privacy Policy** (see PRIVACY_POLICY.md)
6. **Submit for Review**

Processing time: 1-3 days typically

## Updates and Maintenance

For each update:
1. Increment `versionCode` in build.gradle.kts
2. Update `versionName` following semantic versioning
3. Update CHANGELOG.md
4. Build new AAB
5. Upload to Play Console as new release
6. Write release notes
7. Submit for review

## Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [AdMob Documentation](https://developers.google.com/admob/android/quick-start)

## Support

For build issues, open an issue on GitHub or email: dev@minicount.app
