package com.minicount.app.domain.notifications

import android.content.Context
import androidx.work.*
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules notifications for events using WorkManager
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val WORK_TAG_PREFIX = "event_notification_"
        private const val WORK_TAG_EVENT_PREFIX = "event_"
    }

    /**
     * Schedule a notification for an event
     */
    fun scheduleEventNotification(event: Event, daysBeforeEvent: Int) {
        val notificationTime = event.targetDate.minusDays(daysBeforeEvent.toLong())
        val now = LocalDateTime.now()

        // Don't schedule if the time has already passed
        if (notificationTime.isBefore(now)) {
            return
        }

        val delay = Duration.between(now, notificationTime).toMillis()

        val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "eventId" to event.id,
                    "daysUntil" to daysBeforeEvent
                )
            )
            .addTag("${WORK_TAG_PREFIX}${event.id}_$daysBeforeEvent")
            .addTag("${WORK_TAG_EVENT_PREFIX}${event.id}")
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "${WORK_TAG_PREFIX}${event.id}_$daysBeforeEvent",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Schedule multiple reminders for an event
     */
    fun scheduleEventReminders(event: Event, reminders: List<EventReminder>) {
        reminders.filter { it.isEnabled }.forEach { reminder ->
            scheduleEventNotification(event, reminder.daysBefore)
        }
    }

    /**
     * Schedule notification for when an event occurs
     */
    fun scheduleEventOccurredNotification(event: Event) {
        val notificationTime = event.targetDate
        val now = LocalDateTime.now()

        // Don't schedule if the time has already passed
        if (notificationTime.isBefore(now)) {
            return
        }

        val delay = Duration.between(now, notificationTime).toMillis()

        val workRequest = OneTimeWorkRequestBuilder<EventOccurredWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf("eventId" to event.id)
            )
            .addTag("${WORK_TAG_PREFIX}${event.id}_occurred")
            .addTag("${WORK_TAG_EVENT_PREFIX}${event.id}")
            .build()

        workManager.enqueueUniqueWork(
            "${WORK_TAG_PREFIX}${event.id}_occurred",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel all notifications for an event
     */
    fun cancelEventNotifications(eventId: Long) {
        workManager.cancelAllWorkByTag("${WORK_TAG_EVENT_PREFIX}$eventId")
    }

    /**
     * Cancel a specific notification
     */
    fun cancelNotification(eventId: Long, daysBeforeEvent: Int) {
        workManager.cancelUniqueWork("${WORK_TAG_PREFIX}${eventId}_$daysBeforeEvent")
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(WORK_TAG_PREFIX)
    }

    /**
     * Reschedule all notifications for an event (useful after event update)
     */
    fun rescheduleEventNotifications(event: Event, reminders: List<EventReminder>) {
        cancelEventNotifications(event.id)
        scheduleEventReminders(event, reminders)
        scheduleEventOccurredNotification(event)
    }
}

/**
 * Worker for event reminder notifications
 */
class EventNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // This will be implemented with actual notification sending logic
        // For now, it's a placeholder
        return Result.success()
    }
}

/**
 * Worker for event occurred notifications
 */
class EventOccurredWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // This will be implemented with actual notification sending logic
        // For now, it's a placeholder
        return Result.success()
    }
}
