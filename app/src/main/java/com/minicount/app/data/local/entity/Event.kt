package com.minicount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
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

enum class WidgetStyle(
    val displayName: String,
    val isPremium: Boolean = false,
    val price: String = "",
    val description: String = ""
) {
    // Free Styles
    CLASSIC("Classic", false, "", "Traditional countdown display"),
    MINIMAL("Minimal", false, "", "Clean and simple"),
    BOLD("Bold", false, "", "Large, eye-catching numbers"),
    ELEGANT("Elegant", false, "", "Sophisticated and refined"),
    MODERN("Modern", false, "", "Material Design 3"),
    GRADIENT("Gradient", false, "", "Colorful gradients"),

    // Premium Styles ($0.99 each)
    NEON("Neon", true, "$0.99", "Glowing neon effect"),
    GLASS("Glassmorphism", true, "$0.99", "Frosted glass with blur"),
    NEURO("Neumorphism", true, "$0.99", "Soft shadows, raised elements"),
    RETRO("Retro", true, "$0.99", "Vintage flip clock style"),
    COSMIC("Cosmic", true, "$0.99", "Space theme with stars"),
    NATURE("Nature", true, "$0.99", "Organic shapes, earthy tones"),
    LUXURY("Luxury", true, "$0.99", "Gold/silver premium feel"),
    HANDWRITTEN("Handwritten", true, "$0.99", "Personal script fonts"),
    CYBERPUNK("Cyberpunk", true, "$0.99", "Futuristic tech-inspired"),
    MINIMALIST_PRO("Minimalist Pro", true, "$0.99", "Ultra-clean with animations");

    companion object {
        fun getFreeStyles() = values().filter { !it.isPremium }
        fun getPremiumStyles() = values().filter { it.isPremium }
    }
}
