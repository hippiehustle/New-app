package com.minicount.app.domain.notifications

import android.content.Context
import androidx.work.*
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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
@androidx.hilt.work.HiltWorker
class EventNotificationWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted context: Context,
    @dagger.assisted.Assisted params: WorkerParameters,
    private val eventRepository: com.minicount.app.data.repository.EventRepository,
    private val notificationHandler: NotificationHandler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val eventId = inputData.getLong("eventId", -1L)
            val daysUntil = inputData.getInt("daysUntil", 0)

            if (eventId == -1L) {
                android.util.Log.e("EventNotificationWorker", "Invalid event ID")
                return Result.failure()
            }

            // Fetch the event
            val event = eventRepository.getEventById(eventId).first()
            if (event == null) {
                android.util.Log.e("EventNotificationWorker", "Event not found: $eventId")
                return Result.failure()
            }

            // Check if event is still in the future
            if (event.targetDate.isAfter(LocalDateTime.now())) {
                notificationHandler.sendEventReminder(event, daysUntil)
                Result.success()
            } else {
                android.util.Log.d("EventNotificationWorker", "Event has passed: $eventId")
                Result.success()
            }
        } catch (e: Exception) {
            android.util.Log.e("EventNotificationWorker", "Failed to send notification", e)
            Result.retry()
        }
    }
}

/**
 * Worker for event occurred notifications
 */
@androidx.hilt.work.HiltWorker
class EventOccurredWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted context: Context,
    @dagger.assisted.Assisted params: WorkerParameters,
    private val eventRepository: com.minicount.app.data.repository.EventRepository,
    private val notificationHandler: NotificationHandler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val eventId = inputData.getLong("eventId", -1L)

            if (eventId == -1L) {
                android.util.Log.e("EventOccurredWorker", "Invalid event ID")
                return Result.failure()
            }

            // Fetch the event
            val event = eventRepository.getEventById(eventId).first()
            if (event == null) {
                android.util.Log.e("EventOccurredWorker", "Event not found: $eventId")
                return Result.failure()
            }

            // Send notification
            notificationHandler.sendEventOccurred(event)

            // If event is repeating, schedule next occurrence
            if (event.isRepeating && event.repeatInterval != com.minicount.app.data.local.entity.RepeatInterval.NONE) {
                val nextDate = com.minicount.app.domain.util.CountdownCalculator.getNextOccurrence(
                    event.targetDate,
                    event.repeatInterval
                )

                // Update event with next occurrence
                val updatedEvent = event.copy(targetDate = nextDate)
                eventRepository.updateEvent(updatedEvent)

                android.util.Log.d("EventOccurredWorker", "Updated repeating event to next occurrence: $nextDate")
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("EventOccurredWorker", "Failed to send occurrence notification", e)
            Result.retry()
        }
    }
}
