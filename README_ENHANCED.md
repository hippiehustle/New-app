# MiniCount - Event Countdown Widget (Enhanced Edition)

Beautiful countdown widgets for important life events with custom photo backgrounds.

**Version**: 1.5.0 (Enhanced)
**Status**: ✅ Production Ready with Advanced Features

---

## 🎉 What's New in v1.5 (Enhanced Edition)

This enhanced version includes **30+ new features** beyond the original v1.0 release:

### 🚀 Major New Features
- ✨ **Advanced Search & Filtering** - Find events instantly
- 📊 **Statistics Dashboard** - Track your event metrics
- 🔄 **Backup & Export System** - JSON export/import
- 📤 **Event Sharing** - Share as text or beautiful images
- ⚙️ **Settings Screen** - Comprehensive app configuration
- 👋 **Onboarding Experience** - Guided tour for new users
- 🎯 **Multiple Photos** - Photo galleries per event
- 📝 **Event Templates** - Quick event creation
- ⏰ **Multiple Reminders** - Customizable alerts per event
- 🎨 **Widget Configuration** - Personalize each widget
- ♿ **Accessibility** - Full screen reader support
- 🧪 **Unit Tests** - Quality assurance coverage

### 💎 Quality Improvements
- Smooth animations and haptic feedback
- Centralized error handling
- App shortcuts for quick actions
- Enhanced database with migrations
- Professional polish throughout

See [ENHANCEMENTS.md](ENHANCEMENTS.md) for complete details.

---

## 📱 Core Features (v1.0 + v1.5)

### Event Management
- ✨ Unlimited event countdowns (Premium) / 3 events (Free)
- 🖼️ Custom photo backgrounds & photo galleries
- 🎨 12+ event categories with emoji icons
- 🔄 Repeating events (birthdays, anniversaries)
- 📌 Pin important events to top
- 🔍 **NEW:** Search and filter events
- 📊 **NEW:** Event statistics and insights
- 📝 **NEW:** Event templates for quick creation

### Home Screen Widgets
- 📱 3 widget sizes (Small 2x2, Medium 3x2, Large 4x3)
- 🎭 6 widget styles (Classic, Minimal, Bold, Elegant, Modern, Gradient)
- 🎨 8 color themes
- ⏱️ Auto-updating countdown (every 30 minutes)
- ⚙️ **NEW:** Per-widget customization
- 🔧 **NEW:** Configurable tap actions

### Notifications & Reminders
- 🔔 Customizable reminder notifications
- ⏰ **NEW:** Multiple reminders per event
- ⏲️ **NEW:** Specific time settings
- 🎉 Special event day notifications

### Sharing & Backup
- 📤 **NEW:** Share events as text or images
- 🎨 **NEW:** Beautiful countdown image generation
- 💾 **NEW:** Export/import to JSON
- ☁️ **NEW:** Complete data backup system

### Premium Features
- ∞ Unlimited events (vs 3 free)
- 🚫 No ads
- 🎨 Premium themes
- 🎯 Priority support
- 💰 $1.99 one-time payment (no subscription!)

---

## 🏗️ Technical Architecture

### Modern Android Stack
- **Language**: 100% Kotlin (~5,250 lines of code)
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (SQLite) with migrations
- **Widgets**: Glance (Compose for Widgets)
- **DI**: Hilt
- **Background**: WorkManager
- **Images**: Coil
- **Testing**: JUnit + Mockito

### Project Structure
```
app/
├── data/
│   ├── local/
│   │   ├── dao/               # 5 DAOs (Event, Photo, Reminder, Template, WidgetConfig)
│   │   ├── entity/            # 5 entities
│   │   └── Converters.kt      # Type converters
│   ├── preferences/           # DataStore
│   └── repository/            # Data repositories
├── domain/
│   ├── billing/              # In-app purchases
│   ├── notification/         # Push notifications
│   ├── export/               # 🆕 Backup manager
│   ├── share/                # 🆕 Share manager
│   ├── error/                # 🆕 Error handler
│   └── util/                 # Countdown calculator
├── presentation/
│   ├── screens/
│   │   ├── home/             # Main screen + enhanced version
│   │   ├── addedit/          # Add/Edit event
│   │   ├── premium/          # Premium upgrade
│   │   ├── settings/         # 🆕 Settings screen
│   │   ├── statistics/       # 🆕 Statistics dashboard
│   │   └── onboarding/       # 🆕 Onboarding flow
│   ├── components/           # 🆕 Accessibility components
│   ├── navigation/           # Navigation
│   └── theme/                # Material 3 theming
└── widget/                   # Home screen widgets

test/
└── domain/
    ├── util/                 # 🆕 Countdown calculator tests
    └── export/               # 🆕 Backup manager tests
```

---

## 📊 Code Statistics

### Enhanced Version (v1.5)
- **Total Kotlin Files**: 40
- **Total Lines of Code**: ~5,250
- **New Features Added**: 30+
- **New Entities**: 4 (EventPhoto, EventReminder, EventTemplate, WidgetConfig)
- **New Screens**: 3 (Settings, Statistics, Onboarding)
- **New Managers**: 3 (BackupManager, ShareManager, ErrorHandler)
- **Test Files**: 2
- **Test Cases**: 10+

---

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK API 34
- Physical device or emulator

### Building

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd MiniCount
   ```

2. **Open in Android Studio**
   - File → Open → Select MiniCount directory
   - Wait for Gradle sync

3. **Run on device**
   ```bash
   ./gradlew assembleDebug
   # or click Run in Android Studio
   ```

4. **Run tests**
   ```bash
   ./gradlew test
   ```

See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for detailed build guide.

---

## 🎯 New Features Showcase

### 1. Advanced Search & Filtering
```kotlin
// Real-time search
SearchBar(
    query = searchQuery,
    onQueryChange = { query -> /* instant results */ }
)

// Multiple sort options
SortOption: PINNED_FIRST, DATE_ASC, DATE_DESC, NAME_ASC, NAME_DESC, CATEGORY
```

### 2. Event Sharing
```kotlin
// Share as text
shareManager.shareEventAsText(event)

// Share as beautiful image (1080x1080 PNG)
shareManager.shareEventAsImage(event)
```

### 3. Backup & Export
```kotlin
// Export all events to JSON
backupManager.exportToFile(uri)

// Import events from JSON
backupManager.importFromFile(uri)
```

### 4. Statistics Dashboard
- Total events count
- Upcoming vs past events
- Category breakdown
- Next event insights

### 5. Accessibility
```kotlin
// All components with proper semantics
AccessibleIconButton(
    onClick = { /* action */ },
    contentDescription = "Delete event",
    icon = { Icon(Icons.Default.Delete) }
)
```

---

## 🎨 UI/UX Enhancements

### Animations
- Spring-based card animations
- Smooth list transitions
- Expand/collapse animations
- Search result animations

### Haptic Feedback
- Button presses
- Toggle switches
- Long press actions
- Contextual vibrations

### Accessibility
- Full TalkBack support
- Content descriptions
- Semantic roles
- WCAG 2.1 Level AA compliant

---

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests (future)
./gradlew connectedAndroidTest
```

### Test Coverage
- ✅ Countdown calculations
- ✅ Date formatting
- ✅ Repeat intervals
- ✅ Backup data structures
- 🔄 UI tests (planned)

See [TESTING.md](TESTING.md) for comprehensive testing guide.

---

## 📚 Documentation

### Core Documentation
- **README.md** - This file
- **ENHANCEMENTS.md** - Complete feature enhancements guide
- **BUILD_INSTRUCTIONS.md** - Build and release guide
- **TESTING.md** - Testing procedures
- **PLAY_STORE_LISTING.md** - Store listing content

### Additional Documentation
- **PRIVACY_POLICY.md** - Privacy policy
- **CONTRIBUTING.md** - Contribution guidelines
- **CHANGELOG.md** - Version history
- **PROJECT_SUMMARY.md** - Executive summary

---

## 🎯 Use Cases

### Personal Events
- Wedding countdowns
- Birthday trackers
- Anniversary reminders
- Vacation planning
- Graduation dates

### Professional Events
- Project deadlines
- Meeting schedules
- Product launches
- Conference dates
- Certification exams

### Life Milestones
- Baby due dates
- Retirement countdown
- Moving dates
- First day at work
- Important appointments

---

## 💰 Monetization

### Free Tier
- 3 active events
- All widget styles
- All features
- Ad-supported

### Premium ($1.99 one-time)
- Unlimited events
- Ad-free experience
- All current & future premium features
- Lifetime access
- **No subscription required!**

---

## 🔐 Privacy & Security

### Privacy First
- ✅ All data stored locally
- ✅ No cloud sync (optional in future)
- ✅ No account required
- ✅ Minimal permissions
- ✅ GDPR compliant
- ✅ Complete data export capability

### Permissions Required
- **POST_NOTIFICATIONS** - Event reminders
- **READ_MEDIA_IMAGES** - Photo backgrounds (Android 13+)
- **READ_EXTERNAL_STORAGE** - Photo backgrounds (Android 12-)
- **INTERNET** - Ads (free version only)
- **ACCESS_NETWORK_STATE** - Ad connectivity

---

## 🤝 Contributing

We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Ways to Contribute
- 🐛 Report bugs
- 💡 Suggest features
- 🔧 Submit pull requests
- 📝 Improve documentation
- 🌍 Add translations (future)

---

## 📜 License

MIT License - see [LICENSE](LICENSE) file.

---

## 🙏 Acknowledgments

### Built With
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - Dependency injection
- [Glance](https://developer.android.com/jetpack/androidx/releases/glance) - Widgets
- [Coil](https://coil-kt.github.io/coil/) - Image loading
- [Material Design 3](https://m3.material.io/) - Design system

---

## 📞 Support

- **Email**: support@minicount.app
- **Bug Reports**: GitHub Issues
- **Feature Requests**: GitHub Discussions
- **Privacy Questions**: privacy@minicount.app

---

## 🗺️ Roadmap

### v1.6 (Planned - Q1 2025)
- [ ] Photo editing capabilities
- [ ] Event folders/tags
- [ ] More widget styles
- [ ] Custom fonts
- [ ] Tablet optimization

### v2.0 (Planned - Q2 2025)
- [ ] iOS version
- [ ] Cloud sync (optional)
- [ ] Collaboration features
- [ ] Wear OS support
- [ ] Web dashboard

---

## ⭐ Star History

If you find MiniCount useful, please consider:
- ⭐ Starring the repository
- 📱 Leaving a review on Play Store
- 📣 Sharing with friends
- 💬 Providing feedback

---

## 📊 Project Status

### Version History
- **v1.0.0** (Initial) - Core countdown features
- **v1.5.0** (Enhanced) - 30+ new features ✨

### Current Status
- ✅ Fully functional
- ✅ Production ready
- ✅ Actively maintained
- ✅ Open for contributions

### Next Milestone
- 🎯 Play Store launch
- 🎯 1,000 users
- 🎯 4.5+ star rating
- 🎯 Community building

---

**Made with ❤️ for counting down to life's special moments**

---

## Quick Links

- [Features](#-core-features-v10--v15)
- [Architecture](#-technical-architecture)
- [Getting Started](#-quick-start)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [Support](#-support)

---

**MiniCount** - Never miss another special moment 🎉
