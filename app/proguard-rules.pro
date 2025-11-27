# MiniCount Countdown App - ProGuard Rules
# Optimized for production release builds

# ===================================
# Google Play Billing
# ===================================
-keep class com.android.billingclient.** { *; }
-keepclassmembers class com.android.billingclient.** {
    *;
}

# ===================================
# Room Database
# ===================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-dontwarn androidx.room.paging.**

# Keep entity classes and their fields
-keepclassmembers class com.minicount.app.data.local.entity.** {
    *;
}

# ===================================
# Google AdMob
# ===================================
-keep class com.google.android.gms.ads.** { *; }
-keepclassmembers class com.google.android.gms.ads.** {
    *;
}
-dontwarn com.google.android.gms.ads.**

# ===================================
# Kotlin Serialization
# ===================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.minicount.app.**$$serializer { *; }
-keepclassmembers class com.minicount.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.minicount.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ===================================
# Hilt/Dagger
# ===================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keepclasseswithmembernames class * {
    @dagger.* <methods>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <methods>;
}

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# ===================================
# Kotlin Coroutines
# ===================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ===================================
# Glance Widgets
# ===================================
-keep class androidx.glance.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.glance.** {
    *;
}

# ===================================
# Jetpack Compose
# ===================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    *;
}
-dontwarn androidx.compose.**

# ===================================
# Data Classes, Enums, and Sealed Classes
# ===================================
-keepclassmembers class com.minicount.app.data.local.entity.** {
    *;
}
-keepclassmembers class com.minicount.app.presentation.** {
    *;
}

# Keep sealed classes
-keep class com.minicount.app.presentation.common.UiState { *; }
-keep class com.minicount.app.presentation.common.UiState$* { *; }
-keep class com.minicount.app.presentation.common.ActionState { *; }
-keep class com.minicount.app.presentation.common.ActionState$* { *; }
-keep class com.minicount.app.domain.billing.PurchaseState { *; }
-keep class com.minicount.app.domain.billing.PurchaseState$* { *; }

# Keep enums
-keepclassmembers enum com.minicount.app.data.local.entity.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===================================
# WorkManager
# ===================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    *;
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    *;
}

# ===================================
# DataStore
# ===================================
-keep class androidx.datastore.*.** { *; }

# ===================================
# Coil Image Loading
# ===================================
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# ===================================
# General Android & Kotlin
# ===================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes EnclosingMethod

# Keep parcelers
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===================================
# Optimization Settings
# ===================================
# Enable aggressive optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Optimize for code size and performance
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
