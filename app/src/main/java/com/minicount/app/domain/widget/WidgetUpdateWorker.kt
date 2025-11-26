package com.minicount.app.domain.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.minicount.app.presentation.widget.CountdownWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages periodic widget updates using WorkManager
 */
@Singleton
class WidgetUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "widget_update_periodic"
        private const val WIDGET_FORCE_UPDATE_WORK_NAME = "widget_force_update"
        private const val UPDATE_INTERVAL_MINUTES = 15L
    }

    /**
     * Schedule periodic widget updates
     */
    fun schedulePeriodicUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            UPDATE_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .addTag("widget_update")
            .build()

        workManager.enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Force an immediate widget update
     */
    fun forceUpdateWidgets() {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            WIDGET_FORCE_UPDATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel all widget update work
     */
    fun cancelPeriodicUpdates() {
        workManager.cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }

    /**
     * Check if periodic updates are scheduled
     */
    fun arePeriodicUpdatesScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(WIDGET_UPDATE_WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}

/**
 * Worker that updates all widgets
 */
class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Update all Glance widgets
            CountdownWidget().updateAll(context)

            // Also update individual widgets if needed
            val glanceManager = GlanceAppWidgetManager(context)
            val widgetIds = glanceManager.getGlanceIds(CountdownWidget::class.java)

            if (widgetIds.isNotEmpty()) {
                CountdownWidget().updateAll(context)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

/**
 * Extension to schedule widget updates when events change
 */
object WidgetUpdateScheduler {
    /**
     * Trigger widget update when an event is created, updated, or deleted
     */
    fun scheduleWidgetUpdate(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
