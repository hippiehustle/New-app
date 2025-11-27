package com.minicount.app.domain.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.data.repository.EventReminderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker that periodically syncs all event notifications
 * Runs daily to ensure all event reminders are properly scheduled
 */
@HiltWorker
class NotificationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventRepository: EventRepository,
    private val reminderRepository: EventReminderRepository,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "NotificationSyncWorker"
        private const val WORK_NAME = "notification_sync"

        /**
         * Schedule periodic notification sync
         * Runs once per day to ensure all notifications are up-to-date
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationSyncWorker>(
                24, TimeUnit.HOURS,
                30, TimeUnit.MINUTES // flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Log.d(TAG, "Periodic notification sync scheduled")
        }

        /**
         * Trigger immediate notification sync
         */
        fun syncNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationSyncWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Immediate notification sync triggered")
        }

        /**
         * Cancel all notification sync work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Notification sync cancelled")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting notification sync")

            // Get all active events (not completed/archived)
            val events = eventRepository.getAllEvents().first()
                .filter { !it.isCompleted }

            var scheduledCount = 0
            var skippedCount = 0

            events.forEach { event ->
                try {
                    // Get reminders for this event
                    val reminders = reminderRepository.getRemindersForEvent(event.id).first()

                    // Schedule reminders
                    if (reminders.isNotEmpty()) {
                        notificationScheduler.rescheduleEventNotifications(event, reminders)
                        scheduledCount++
                    } else {
                        // Schedule default reminder if none configured
                        notificationScheduler.scheduleEventNotification(event, event.notificationDaysBefore)
                        notificationScheduler.scheduleEventOccurredNotification(event)
                        scheduledCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to schedule notifications for event ${event.id}: ${event.title}", e)
                    skippedCount++
                }
            }

            Log.d(TAG, "Notification sync complete: $scheduledCount scheduled, $skippedCount skipped")

            // Output metrics for debugging
            setProgressAsync(
                workDataOf(
                    "scheduled_count" to scheduledCount,
                    "skipped_count" to skippedCount,
                    "total_events" to events.size
                )
            )

            Result.success(
                workDataOf(
                    "scheduled_count" to scheduledCount,
                    "skipped_count" to skippedCount,
                    "total_events" to events.size
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Notification sync failed", e)

            // Retry with exponential backoff
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
}
