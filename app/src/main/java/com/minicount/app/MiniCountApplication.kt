package com.minicount.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.minicount.app.domain.notifications.NotificationSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MiniCountApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        MobileAds.initialize(this)

        // Create notification channels
        createNotificationChannels()

        // Schedule periodic notification sync (daily)
        NotificationSyncWorker.schedulePeriodic(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_EVENT_REMINDER,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for upcoming events"
                },
                NotificationChannel(
                    CHANNEL_EVENT_TODAY,
                    "Events Today",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for events happening today"
                }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_EVENT_REMINDER = "event_reminder"
        const val CHANNEL_EVENT_TODAY = "event_today"
    }
}
