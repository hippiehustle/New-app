package com.minicount.app.domain.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.minicount.app.MiniCountApplication
import com.minicount.app.R
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.util.CountdownCalculator
import com.minicount.app.presentation.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepository: EventRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val events = eventRepository.getEventsWithNotifications()

        events.forEach { event ->
            val targetDate = if (event.isRepeating) {
                CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
            } else {
                event.targetDate
            }

            val daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), targetDate)

            // Send notification if event is within notification window
            if (daysUntil in 0..event.notificationDaysBefore.toLong()) {
                sendNotification(event.id.toInt(), event.title, targetDate, daysUntil)
            }

            // Send notification if event is today
            if (daysUntil == 0L) {
                sendTodayNotification(event.id.toInt(), event.title)
            }
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, targetDate: LocalDateTime, daysUntil: Long) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when (daysUntil) {
            0L -> "is today!"
            1L -> "is tomorrow!"
            else -> "is in $daysUntil days"
        }

        val notification = NotificationCompat.Builder(applicationContext, MiniCountApplication.CHANNEL_EVENT_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Your event $message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }

    private fun sendTodayNotification(id: Int, title: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, MiniCountApplication.CHANNEL_EVENT_TODAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$title is today! 🎉")
            .setContentText("Your special day has arrived")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id + 10000, notification)
    }

    companion object {
        fun scheduleNotificationCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
                12, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notification_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
            )
        }
    }
}
