# Testing Guide for MiniCount

Comprehensive testing procedures for ensuring quality before release.

## Testing Types

### 1. Unit Tests
### 2. Integration Tests
### 3. UI Tests
### 4. Manual Tests
### 5. Performance Tests
### 6. Compatibility Tests

---

## 1. Unit Tests

Testing individual components in isolation.

### Running Unit Tests
```bash
./gradlew test
./gradlew testDebugUnitTest
```

### Key Areas to Test

#### CountdownCalculator Tests
```kotlin
// Test countdown calculation
- Correct days calculation
- Correct hours/minutes/seconds
- Past events (count up mode)
- Leap year handling
- Time zone handling
- Repeating event next occurrence
```

#### Repository Tests
```kotlin
// Test data layer
- Insert event
- Update event
- Delete event
- Query events
- Event count
- Sort order (pinned events first)
```

#### ViewModel Tests
```kotlin
// Test business logic
- Event creation flow
- Form validation
- Premium check logic
- Widget count tracking
```

### Coverage Goal
- Minimum 70% code coverage
- 100% coverage for critical business logic

---

## 2. Integration Tests

Testing components working together.

### Database Tests
```bash
./gradlew connectedAndroidTest
```

Test scenarios:
- [ ] Create event and retrieve from database
- [ ] Update event reflects in database
- [ ] Delete event removes from database
- [ ] Query filters work correctly
- [ ] Room type converters work
- [ ] Database migrations (future)

### Repository + Database Tests
- [ ] EventRepository correctly interfaces with DAO
- [ ] Flow updates propagate correctly
- [ ] Concurrent operations handled

---

## 3. UI Tests (Instrumentation)

Testing user interface with Espresso/Compose Testing.

### Running UI Tests
```bash
./gradlew connectedAndroidTest
```

### Test Scenarios

#### Home Screen
- [ ] Empty state displays when no events
- [ ] Events list displays correctly
- [ ] Add event button works
- [ ] Event cards show correct information
- [ ] Delete event dialog appears
- [ ] Pinned events appear first
- [ ] Premium banner shows for free users

#### Add/Edit Event Screen
- [ ] All input fields accept text
- [ ] Date picker opens and sets date
- [ ] Time picker opens and sets time
- [ ] Category selection works
- [ ] Photo picker opens
- [ ] Save button creates/updates event
- [ ] Validation shows errors
- [ ] Back button navigates correctly

#### Premium Screen
- [ ] Feature list displays
- [ ] Purchase button triggers billing
- [ ] Premium status shows correctly

---

## 4. Manual Testing Checklist

### Pre-Release Testing

#### Installation
- [ ] Clean install works
- [ ] App launches successfully
- [ ] No crashes on first launch
- [ ] Permissions requested appropriately

#### Event Management
- [ ] Create new event
  - [ ] Enter title (required)
  - [ ] Enter description (optional)
  - [ ] Select date (past, present, future)
  - [ ] Select time
  - [ ] Choose category
  - [ ] Add photo background
  - [ ] Enable repeating
  - [ ] Set notification
  - [ ] Choose color
  - [ ] Select widget style
  - [ ] Save successfully

- [ ] Edit existing event
  - [ ] All fields populate correctly
  - [ ] Changes save
  - [ ] Cancel discards changes

- [ ] Delete event
  - [ ] Confirmation dialog appears
  - [ ] Delete removes event
  - [ ] Cancel keeps event

#### Widgets
- [ ] Add widget to home screen
  - [ ] Small widget (2x2)
  - [ ] Medium widget (3x2)
  - [ ] Large widget (4x3)

- [ ] Widget displays correctly
  - [ ] Event title shows
  - [ ] Countdown updates
  - [ ] Photo background displays (if set)
  - [ ] Category emoji shows
  - [ ] Color theme applies
  - [ ] Style renders correctly

- [ ] Widget updates
  - [ ] Updates every 30 minutes
  - [ ] Manual update works
  - [ ] Countdown decrements correctly

- [ ] Widget interactions
  - [ ] Tap opens app
  - [ ] Long press shows options
  - [ ] Resize works
  - [ ] Remove works

- [ ] Multiple widgets
  - [ ] Can add multiple widgets
  - [ ] Each shows different event (future feature)
  - [ ] All update independently

#### Notifications
- [ ] Grant notification permission
- [ ] Notification appears at correct time
- [ ] Notification content correct
- [ ] Tap notification opens app
- [ ] Dismiss notification works
- [ ] Event day notification special
- [ ] Notification settings persist

#### Freemium Model
- [ ] Free version allows 3 events
- [ ] 4th event prompts upgrade
- [ ] Event counter accurate
- [ ] Premium banner shows
- [ ] Banner disappears for premium

#### Premium Purchase
- [ ] Premium screen opens
- [ ] Features listed correctly
- [ ] Purchase flow initiates
- [ ] Test purchase works (in test mode)
- [ ] Purchase restores correctly
- [ ] Premium features unlock
- [ ] Ads disappear
- [ ] Unlimited events enabled

#### Ads (Free Version)
- [ ] Test ad displays
- [ ] Ad loads correctly
- [ ] Ad doesn't block UI
- [ ] No ads for premium users

#### UI/UX
- [ ] Material Design 3 theme
- [ ] Colors consistent
- [ ] Typography readable
- [ ] Icons clear
- [ ] Animations smooth
- [ ] Transitions natural
- [ ] Loading states show
- [ ] Error states clear

#### Dark Mode
- [ ] Switch to dark mode
- [ ] UI elements visible
- [ ] Contrast sufficient
- [ ] Widgets adapt
- [ ] Dynamic colors work (Android 12+)

#### Orientation
- [ ] Portrait mode works
- [ ] Landscape mode works
- [ ] Rotation preserves state
- [ ] Layouts adapt

#### Edge Cases
- [ ] Very long event title
- [ ] Very long description
- [ ] Far future date (year 2100)
- [ ] Far past date (year 1900)
- [ ] Today's date
- [ ] Same minute (countdown 0)
- [ ] Special characters in title
- [ ] Emoji in title
- [ ] Multiple events same date
- [ ] Delete all events
- [ ] No photo selected
- [ ] Low storage space
- [ ] No internet (for ads)

---

## 5. Performance Testing

### Metrics to Measure

#### App Size
- [ ] APK size < 20MB
- [ ] AAB size < 15MB
- Target: Under 10MB

#### Startup Time
- [ ] Cold start < 2 seconds
- [ ] Warm start < 1 second
- [ ] Hot start < 0.5 seconds

#### Memory Usage
- [ ] Baseline < 50MB
- [ ] With 100 events < 100MB
- [ ] No memory leaks

#### Battery Impact
- [ ] Background battery usage minimal
- [ ] Widget updates efficient
- [ ] WorkManager not excessive

#### Database Performance
- [ ] 1000 events query < 100ms
- [ ] Insert event < 50ms
- [ ] Update event < 50ms
- [ ] Delete event < 50ms

### Performance Testing Tools
```bash
# Memory profiler
adb shell dumpsys meminfo com.minicount.app

# CPU profiler
adb shell top | grep minicount

# Battery stats
adb shell dumpsys batterystats
```

---

## 6. Compatibility Testing

### Android Versions
Test on:
- [ ] Android 14 (API 34) - Latest
- [ ] Android 13 (API 33)
- [ ] Android 12 (API 31) - Dynamic colors
- [ ] Android 11 (API 30)
- [ ] Android 10 (API 29)
- [ ] Android 9 (API 28)
- [ ] Android 8.0 (API 26) - Minimum supported

### Device Types
- [ ] Phone (small) - 5" screen
- [ ] Phone (medium) - 6" screen
- [ ] Phone (large) - 6.5"+ screen
- [ ] Tablet (7")
- [ ] Tablet (10")
- [ ] Foldable devices

### Manufacturers
- [ ] Google Pixel
- [ ] Samsung Galaxy
- [ ] OnePlus
- [ ] Xiaomi
- [ ] Others (various)

### Screen Densities
- [ ] LDPI (120 dpi)
- [ ] MDPI (160 dpi)
- [ ] HDPI (240 dpi)
- [ ] XHDPI (320 dpi)
- [ ] XXHDPI (480 dpi)
- [ ] XXXHDPI (640 dpi)

---

## 7. Security Testing

### Permissions
- [ ] Only necessary permissions requested
- [ ] Permission rationale shown
- [ ] Graceful degradation if denied
- [ ] No sensitive data in logs

### Data Security
- [ ] Local data encrypted (if applicable)
- [ ] No data leakage
- [ ] Secure photo access
- [ ] No SQL injection vulnerabilities

### Third-Party SDKs
- [ ] AdMob SDK updated
- [ ] Billing library updated
- [ ] No known vulnerabilities

---

## 8. Accessibility Testing

### Screen Reader
- [ ] All elements have content descriptions
- [ ] Navigation logical
- [ ] Announcements clear

### Font Scaling
- [ ] UI readable at 200% font size
- [ ] No text cutoff
- [ ] Layouts adapt

### Color Contrast
- [ ] Meets WCAG AA standards
- [ ] Readable in sunlight
- [ ] Color-blind friendly

---

## 9. Localization Testing

(For future releases)

- [ ] All strings externalized
- [ ] RTL languages supported
- [ ] Date/time formats localized
- [ ] Currency symbols correct
- [ ] No hardcoded strings

---

## 10. Regression Testing

After each update, retest:
- [ ] All critical user flows
- [ ] Previously fixed bugs
- [ ] Integration points
- [ ] Performance benchmarks

---

## Bug Reporting Template

When finding bugs:

```markdown
**Title**: Brief description

**Severity**: Critical / High / Medium / Low

**Steps to Reproduce**:
1. Step 1
2. Step 2
3. Step 3

**Expected Result**:
What should happen

**Actual Result**:
What actually happens

**Environment**:
- Device: Pixel 6
- Android Version: 14
- App Version: 1.0.0

**Screenshots/Videos**:
[Attach if applicable]

**Logs**:
```
[Paste relevant logs]
```

---

## Test Reports

After testing, generate report:

### Summary
- Total tests: X
- Passed: X
- Failed: X
- Blocked: X
- Pass rate: X%

### Critical Issues
List any blocking issues

### Known Issues
Document non-blocking issues

### Recommendations
Suggestions for improvement

---

## Continuous Testing

### Automated Testing (Future)
- GitHub Actions for every commit
- UI tests on Firebase Test Lab
- Monkey testing for crashes

### Beta Testing
- Internal testing (team)
- Closed beta (trusted users)
- Open beta (public, Play Store)
- Collect feedback

---

## Pre-Release Checklist

Before submitting to Play Store:

- [ ] All tests passing
- [ ] No critical bugs
- [ ] Performance acceptable
- [ ] Compatibility verified
- [ ] Accessibility checked
- [ ] Security reviewed
- [ ] Privacy policy updated
- [ ] Store listing ready
- [ ] Screenshots prepared
- [ ] Release notes written

---

## Post-Release Monitoring

After release:

- [ ] Monitor crash reports (Play Console)
- [ ] Check ANR (App Not Responding) rate
- [ ] Review user ratings
- [ ] Read user reviews
- [ ] Track key metrics
- [ ] Plan hotfixes if needed

Target Metrics:
- Crash-free rate: > 99%
- ANR rate: < 1%
- Average rating: > 4.0 stars

---

## Testing Tools Reference

### Android Studio
- Layout Inspector
- Database Inspector
- Memory Profiler
- CPU Profiler
- Network Profiler

### ADB Commands
```bash
# Install APK
adb install app-debug.apk

# Clear app data
adb shell pm clear com.minicount.app

# View logs
adb logcat | grep MiniCount

# Take screenshot
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png

# Grant permissions
adb shell pm grant com.minicount.app android.permission.POST_NOTIFICATIONS
```

### Firebase
- Crashlytics (crash reporting)
- Performance Monitoring
- Test Lab (cloud testing)

---

## Support

For testing questions: qa@minicount.app
