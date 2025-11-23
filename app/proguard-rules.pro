# Add project specific ProGuard rules here.
# Keep billing library
-keep class com.android.billingclient.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep AdMob
-keep class com.google.android.gms.ads.** { *; }

# Keep data classes
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
