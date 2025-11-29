package com.minicount.app.domain.templates

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import java.time.LocalDateTime

data class EventTemplate(
    val name: String,
    val category: EventCategory,
    val repeatInterval: RepeatInterval,
    val notificationDaysBefore: Int,
    val description: String,
    val color: Int
)

object EventTemplates {
    val ALL_TEMPLATES = listOf(
        EventTemplate("Birthday", EventCategory.BIRTHDAY, RepeatInterval.YEARLY, 7, "Annual birthday", 0xFFE91E63.toInt()),
        EventTemplate("Anniversary", EventCategory.ANNIVERSARY, RepeatInterval.YEARLY, 14, "Anniversary", 0xFFF44336.toInt()),
        EventTemplate("Wedding", EventCategory.WEDDING, RepeatInterval.NONE, 30, "Wedding", 0xFFFFFFFF.toInt()),
        EventTemplate("Vacation", EventCategory.VACATION, RepeatInterval.NONE, 7, "Vacation", 0xFF00BCD4.toInt()),
        EventTemplate("Exam", EventCategory.EXAM, RepeatInterval.NONE, 3, "Exam", 0xFFFF9800.toInt()),
        EventTemplate("Holiday", EventCategory.HOLIDAY, RepeatInterval.YEARLY, 7, "Holiday", 0xFF4CAF50.toInt())
    )
    
    fun createEventFromTemplate(template: EventTemplate, title: String, targetDate: LocalDateTime, notes: String = ""): Event {
        return Event(
            id = 0, title = title, notes = notes, targetDate = targetDate, category = template.category,
            photoUri = null, isRepeating = template.repeatInterval != RepeatInterval.NONE,
            repeatInterval = template.repeatInterval, notificationEnabled = true,
            notificationDaysBefore = template.notificationDaysBefore, createdAt = LocalDateTime.now(),
            color = template.color, widgetStyle = WidgetStyle.CLASSIC, isPinned = false
        )
    }
}
