package com.minicount.app.data.local.dao

import androidx.room.*
import com.minicount.app.data.local.entity.EventPhoto
import com.minicount.app.data.local.entity.EventReminder
import com.minicount.app.data.local.entity.EventTemplate
import com.minicount.app.data.local.entity.WidgetConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface EventPhotoDao {
    @Query("SELECT * FROM event_photos WHERE eventId = :eventId ORDER BY `order` ASC")
    fun getPhotosForEvent(eventId: Long): Flow<List<EventPhoto>>

    @Query("SELECT * FROM event_photos WHERE eventId = :eventId AND isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryPhoto(eventId: Long): EventPhoto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: EventPhoto): Long

    @Update
    suspend fun updatePhoto(photo: EventPhoto)

    @Delete
    suspend fun deletePhoto(photo: EventPhoto)

    @Query("DELETE FROM event_photos WHERE eventId = :eventId")
    suspend fun deletePhotosForEvent(eventId: Long)
}

@Dao
interface EventReminderDao {
    @Query("SELECT * FROM event_reminders WHERE eventId = :eventId ORDER BY daysBefore ASC")
    fun getRemindersForEvent(eventId: Long): Flow<List<EventReminder>>

    @Query("SELECT * FROM event_reminders WHERE eventId = :eventId ORDER BY daysBefore ASC")
    suspend fun getRemindersForEventSync(eventId: Long): List<EventReminder>

    @Query("SELECT * FROM event_reminders WHERE id = :reminderId")
    fun getReminderById(reminderId: Long): Flow<EventReminder?>

    @Query("SELECT * FROM event_reminders WHERE isEnabled = 1")
    suspend fun getEnabledReminders(): List<EventReminder>

    @Query("SELECT * FROM event_reminders WHERE isEnabled = 1")
    suspend fun getAllEnabledReminders(): List<EventReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: EventReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<EventReminder>)

    @Update
    suspend fun updateReminder(reminder: EventReminder)

    @Delete
    suspend fun deleteReminder(reminder: EventReminder)

    @Query("DELETE FROM event_reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)

    @Query("DELETE FROM event_reminders WHERE eventId = :eventId")
    suspend fun deleteRemindersForEvent(eventId: Long)

    @Query("SELECT COUNT(*) FROM event_reminders WHERE eventId = :eventId")
    fun getReminderCountForEvent(eventId: Long): Flow<Int>
}

@Dao
interface EventTemplateDao {
    @Query("SELECT * FROM event_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<EventTemplate>>

    @Query("SELECT * FROM event_templates WHERE id = :templateId")
    suspend fun getTemplate(templateId: Long): EventTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: EventTemplate): Long

    @Update
    suspend fun updateTemplate(template: EventTemplate)

    @Delete
    suspend fun deleteTemplate(template: EventTemplate)
}

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_config WHERE widgetId = :widgetId")
    suspend fun getWidgetConfig(widgetId: Int): WidgetConfig?

    @Query("SELECT * FROM widget_config")
    fun getAllWidgetConfigs(): Flow<List<WidgetConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfig(config: WidgetConfig)

    @Update
    suspend fun updateWidgetConfig(config: WidgetConfig)

    @Delete
    suspend fun deleteWidgetConfig(config: WidgetConfig)

    @Query("DELETE FROM widget_config WHERE widgetId = :widgetId")
    suspend fun deleteWidgetConfigById(widgetId: Int)
}
