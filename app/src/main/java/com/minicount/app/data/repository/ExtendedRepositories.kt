package com.minicount.app.data.repository

import com.minicount.app.data.local.dao.EventHistoryDao
import com.minicount.app.data.local.dao.EventPhotoDao
import com.minicount.app.data.local.dao.EventReminderDao
import com.minicount.app.data.local.dao.EventTemplateDao
import com.minicount.app.data.local.dao.WidgetConfigDao
import com.minicount.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventPhotoRepository @Inject constructor(
    private val eventPhotoDao: EventPhotoDao
) {
    fun getPhotosForEvent(eventId: Long): Flow<List<EventPhoto>> =
        eventPhotoDao.getPhotosForEvent(eventId)

    suspend fun getPrimaryPhoto(eventId: Long): EventPhoto? =
        eventPhotoDao.getPrimaryPhoto(eventId)

    suspend fun insertPhoto(photo: EventPhoto): Long =
        eventPhotoDao.insertPhoto(photo)

    suspend fun updatePhoto(photo: EventPhoto) =
        eventPhotoDao.updatePhoto(photo)

    suspend fun deletePhoto(photo: EventPhoto) =
        eventPhotoDao.deletePhoto(photo)

    suspend fun deletePhotosForEvent(eventId: Long) =
        eventPhotoDao.deletePhotosForEvent(eventId)
}

@Singleton
class EventReminderRepository @Inject constructor(
    private val eventReminderDao: EventReminderDao
) {
    fun getRemindersForEvent(eventId: Long): Flow<List<EventReminder>> =
        eventReminderDao.getRemindersForEvent(eventId)

    suspend fun insertReminder(reminder: EventReminder): Long =
        eventReminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: EventReminder) =
        eventReminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: EventReminder) =
        eventReminderDao.deleteReminder(reminder)

    suspend fun deleteRemindersForEvent(eventId: Long) =
        eventReminderDao.deleteRemindersForEvent(eventId)

    suspend fun getEnabledReminders(): List<EventReminder> =
        eventReminderDao.getEnabledReminders()
}

@Singleton
class EventTemplateRepository @Inject constructor(
    private val eventTemplateDao: EventTemplateDao
) {
    fun getAllTemplates(): Flow<List<EventTemplate>> =
        eventTemplateDao.getAllTemplates()

    suspend fun getTemplate(templateId: Long): EventTemplate? =
        eventTemplateDao.getTemplate(templateId)

    suspend fun insertTemplate(template: EventTemplate): Long =
        eventTemplateDao.insertTemplate(template)

    suspend fun updateTemplate(template: EventTemplate) =
        eventTemplateDao.updateTemplate(template)

    suspend fun deleteTemplate(template: EventTemplate) =
        eventTemplateDao.deleteTemplate(template)
}

@Singleton
class WidgetConfigRepository @Inject constructor(
    private val widgetConfigDao: WidgetConfigDao
) {
    suspend fun getWidgetConfig(widgetId: Int): WidgetConfig? =
        widgetConfigDao.getWidgetConfig(widgetId)

    fun getAllWidgetConfigs(): Flow<List<WidgetConfig>> =
        widgetConfigDao.getAllWidgetConfigs()

    suspend fun insertWidgetConfig(config: WidgetConfig) =
        widgetConfigDao.insertWidgetConfig(config)

    suspend fun updateWidgetConfig(config: WidgetConfig) =
        widgetConfigDao.updateWidgetConfig(config)

    suspend fun deleteWidgetConfig(config: WidgetConfig) =
        widgetConfigDao.deleteWidgetConfig(config)

    suspend fun deleteWidgetConfigById(widgetId: Int) =
        widgetConfigDao.deleteWidgetConfigById(widgetId)
}

@Singleton
class EventHistoryRepository @Inject constructor(
    private val eventHistoryDao: EventHistoryDao
) {
    fun getAllHistory(): Flow<List<EventHistory>> =
        eventHistoryDao.getAllHistory()

    fun getHistoryForEvent(eventId: Long): Flow<List<EventHistory>> =
        eventHistoryDao.getHistoryForEvent(eventId)

    fun getHistoryInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<EventHistory>> =
        eventHistoryDao.getHistoryInRange(
            startDate.toEpochSecond(java.time.ZoneOffset.UTC),
            endDate.toEpochSecond(java.time.ZoneOffset.UTC)
        )

    suspend fun insertHistory(history: EventHistory): Long =
        eventHistoryDao.insertHistory(history)

    suspend fun updateHistory(history: EventHistory) =
        eventHistoryDao.updateHistory(history)

    suspend fun deleteHistory(history: EventHistory) =
        eventHistoryDao.deleteHistory(history)

    suspend fun deleteHistoryForEvent(eventId: Long) =
        eventHistoryDao.deleteHistoryForEvent(eventId)

    suspend fun getStatisticsForEvent(eventId: Long): EventStatistics? =
        eventHistoryDao.getStatisticsForEvent(eventId)

    suspend fun getOccurrenceCount(eventId: Long): Int =
        eventHistoryDao.getOccurrenceCount(eventId)

    /**
     * Records an event occurrence in history
     */
    suspend fun recordEventOccurrence(event: Event) {
        val history = EventHistory(
            eventId = event.id,
            eventTitle = event.title,
            eventCategory = event.category,
            occurredDate = event.targetDate
        )
        insertHistory(history)
    }
}
