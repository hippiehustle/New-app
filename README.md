# MiniCount - Event Countdown Widget

Beautiful countdown widgets for important life events (weddings, birthdays, vacations, etc.) with custom photo backgrounds.

## Features

### Core Features
- ✨ **Unlimited Event Countdowns** - Track all your important dates
- 📱 **Home Screen Widgets** - Multiple sizes (Small, Medium, Large)
- 🖼️ **Custom Photo Backgrounds** - Use photos from your gallery
- ⏱️ **Count Up After Event** - See how long it's been since past events
- 🎨 **Event Categories** - 12+ categories with emojis (Wedding, Birthday, Anniversary, etc.)
- 🔄 **Repeating Events** - Perfect for birthdays and anniversaries
- 🔔 **Notification Reminders** - Get notified before events
- 🎭 **Multiple Widget Styles** - Classic, Minimal, Bold, Elegant, Modern, Gradient

### Premium Features
- Unlimited events (Free: 3 events)
- No ads
- Premium themes
- Priority support

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room
- **Widgets**: Glance (Jetpack Compose for Widgets)
- **Dependency Injection**: Hilt
- **Background Work**: WorkManager
- **Billing**: Google Play Billing Library
- **Ads**: Google AdMob
- **Image Loading**: Coil

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # Database access objects
│   │   ├── entity/       # Database entities
│   │   └── Converters.kt
│   ├── preferences/      # DataStore preferences
│   └── repository/       # Data repositories
├── domain/
│   ├── billing/         # In-app purchase logic
│   ├── notification/    # Notification workers
│   └── util/            # Utilities (countdown calculator)
├── presentation/
│   ├── screens/
│   │   ├── home/        # Main screen
│   │   ├── addedit/     # Add/Edit event screen
│   │   └── premium/     # Premium upgrade screen
│   ├── navigation/      # Navigation setup
│   ├── theme/           # Material Design 3 theming
│   └── components/      # Reusable components
└── widget/              # Widget implementation
```

## Building the App

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK with API 34

### Build Steps

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd MiniCount
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

### Release Build

1. Create a keystore for signing:
   ```bash
   keytool -genkey -v -keystore minicount.keystore -alias minicount -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in project root:
   ```properties
   storePassword=YOUR_STORE_PASSWORD
   keyPassword=YOUR_KEY_PASSWORD
   keyAlias=minicount
   storeFile=minicount.keystore
   ```

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## Configuration

### AdMob Setup
Replace test ad unit IDs in the following files before publishing:
- `AndroidManifest.xml` - Application ID
- `AdBanner.kt` - Banner ad unit ID

### Google Play Billing
Update the product ID in `BillingManager.kt` to match your Play Console setup.

## Play Store Submission Checklist

- [ ] Replace AdMob test IDs with real ad units
- [ ] Configure Google Play Billing product
- [ ] Generate signed release APK/AAB
- [ ] Create app icon in all densities (done - placeholder PNGs)
- [ ] Create feature graphic (1024x500)
- [ ] Take screenshots for all device types
- [ ] Write store listing (see PLAY_STORE_LISTING.md)
- [ ] Set up privacy policy
- [ ] Complete Play Console questionnaire
- [ ] Submit for review

## Privacy Policy

MiniCount collects minimal user data:
- Event data stored locally on device
- Anonymous analytics via Google Play Services
- Ad targeting data via AdMob (for free users)

Full privacy policy required for Play Store submission.

## License

Copyright © 2024. All rights reserved.

## Support

For support, email: support@minicount.app

## Roadmap

- [ ] iOS version
- [ ] Social sharing features
- [ ] Cloud backup
- [ ] Collaboration features
- [ ] More widget styles
- [ ] Dark mode customization
- [ ] Export/import functionality
