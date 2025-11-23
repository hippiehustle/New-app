package com.minicount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetDate: LocalDateTime,
    val category: EventCategory = EventCategory.OTHER,
    val photoUri: String? = null,
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val notificationEnabled: Boolean = true,
    val notificationDaysBefore: Int = 1,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: Int = 0xFF6200EE.toInt(),
    val widgetStyle: WidgetStyle = WidgetStyle.CLASSIC,
    val isPinned: Boolean = false
)

enum class EventCategory(val icon: String, val displayName: String) {
    WEDDING("💍", "Wedding"),
    BIRTHDAY("🎂", "Birthday"),
    ANNIVERSARY("💕", "Anniversary"),
    VACATION("✈️", "Vacation"),
    GRADUATION("🎓", "Graduation"),
    BABY("👶", "Baby"),
    MEETING("📅", "Meeting"),
    EXAM("📝", "Exam"),
    CONCERT("🎵", "Concert"),
    SPORTS("⚽", "Sports"),
    HOLIDAY("🎄", "Holiday"),
    OTHER("📌", "Other")
}

enum class RepeatInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class WidgetStyle {
    CLASSIC,
    MINIMAL,
    BOLD,
    ELEGANT,
    MODERN,
    GRADIENT
}
