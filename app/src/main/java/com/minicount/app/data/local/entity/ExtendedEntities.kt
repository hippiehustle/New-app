package com.minicount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_photos")
data class EventPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val photoUri: String,
    val isPrimary: Boolean = false,
    val order: Int = 0
)

@Entity(tableName = "event_reminders")
data class EventReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val daysBefore: Int,
    val hourOfDay: Int = 9,
    val minute: Int = 0,
    val isEnabled: Boolean = true
)

@Entity(tableName = "event_templates")
data class EventTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: EventCategory,
    val description: String = "",
    val defaultPhotoUri: String? = null,
    val color: Int,
    val widgetStyle: WidgetStyle,
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val defaultReminderDays: Int = 1
)

@Entity(tableName = "widget_config")
data class WidgetConfig(
    @PrimaryKey
    val widgetId: Int,
    val eventId: Long,
    val showSeconds: Boolean = false,
    val opacity: Float = 1.0f,
    val customFontSize: Int = 0, // 0 = default
    val showCategoryIcon: Boolean = true,
    val showEventName: Boolean = true,
    val tapAction: WidgetTapAction = WidgetTapAction.OPEN_APP
)

enum class WidgetTapAction {
    OPEN_APP,
    OPEN_EVENT,
    MARK_COMPLETE,
    SNOOZE
}
