package com.minicount.app.domain.util

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Extension functions for cleaner code throughout the app
 */

// ========================
// Event Extensions
// ========================

/**
 * Check if an event is upcoming (target date is in the future)
 */
fun Event.isUpcoming(): Boolean = targetDate.isAfter(LocalDateTime.now())

/**
 * Check if an event has passed
 */
fun Event.hasPassed(): Boolean = targetDate.isBefore(LocalDateTime.now())

/**
 * Check if an event is today
 */
fun Event.isToday(): Boolean {
    val today = LocalDate.now()
    return targetDate.toLocalDate() == today
}

/**
 * Get days until/since event
 */
fun Event.daysUntil(): Long {
    return ChronoUnit.DAYS.between(LocalDateTime.now(), targetDate)
}

/**
 * Get hours until/since event
 */
fun Event.hoursUntil(): Long {
    return ChronoUnit.HOURS.between(LocalDateTime.now(), targetDate)
}

/**
 * Get formatted date string
 */
fun Event.formattedDate(pattern: String = "MMM dd, yyyy"): String {
    return targetDate.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * Get formatted date and time string
 */
fun Event.formattedDateTime(pattern: String = "MMM dd, yyyy 'at' HH:mm"): String {
    return targetDate.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * Get event color as Compose Color
 */
fun Event.composeColor(): Color = Color(color)

/**
 * Check if event has a photo
 */
fun Event.hasPhoto(): Boolean = !photoUri.isNullOrBlank()

/**
 * Check if event has notes
 */
fun Event.hasNotes(): Boolean = notes.isNotBlank()

/**
 * Get a short description of the event
 */
fun Event.shortDescription(): String {
    return buildString {
        append(category.icon)
        append(" ")
        append(title)
        if (isRepeating) {
            append(" (Repeating)")
        }
    }
}

// ========================
// LocalDateTime Extensions
// ========================

/**
 * Check if LocalDateTime is in the past
 */
fun LocalDateTime.isPast(): Boolean = this.isBefore(LocalDateTime.now())

/**
 * Check if LocalDateTime is in the future
 */
fun LocalDateTime.isFuture(): Boolean = this.isAfter(LocalDateTime.now())

/**
 * Check if LocalDateTime is today
 */
fun LocalDateTime.isToday(): Boolean = this.toLocalDate() == LocalDate.now()

/**
 * Check if LocalDateTime is tomorrow
 */
fun LocalDateTime.isTomorrow(): Boolean = this.toLocalDate() == LocalDate.now().plusDays(1)

/**
 * Check if LocalDateTime is yesterday
 */
fun LocalDateTime.isYesterday(): Boolean = this.toLocalDate() == LocalDate.now().minusDays(1)

/**
 * Get relative time string (e.g., "2 days ago", "in 3 hours")
 */
fun LocalDateTime.toRelativeString(): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(now, this)
    val days = duration.toDays()
    val hours = duration.toHours()
    val minutes = duration.toMinutes()

    return when {
        this.isToday() -> "Today"
        this.isTomorrow() -> "Tomorrow"
        this.isYesterday() -> "Yesterday"
        days > 0 -> "In $days day${if (days > 1) "s" else ""}"
        days < 0 -> "${-days} day${if (days < -1) "s" else ""} ago"
        hours > 0 -> "In $hours hour${if (hours > 1) "s" else ""}"
        hours < 0 -> "${-hours} hour${if (hours < -1) "s" else ""} ago"
        minutes > 0 -> "In $minutes minute${if (minutes > 1) "s" else ""}"
        minutes < 0 -> "${-minutes} minute${if (minutes < -1) "s" else ""} ago"
        else -> "Just now"
    }
}

/**
 * Format LocalDateTime with pattern
 */
fun LocalDateTime.formatWith(pattern: String): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * Get start of day
 */
fun LocalDateTime.startOfDay(): LocalDateTime {
    return this.toLocalDate().atStartOfDay()
}

/**
 * Get end of day
 */
fun LocalDateTime.endOfDay(): LocalDateTime {
    return this.toLocalDate().atTime(23, 59, 59)
}

// ========================
// String Extensions
// ========================

/**
 * Truncate string to max length with ellipsis
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Capitalize first letter of each word
 */
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Check if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Check if string contains only digits
 */
fun String.isNumeric(): Boolean {
    return this.all { it.isDigit() }
}

// ========================
// Collection Extensions
// ========================

/**
 * Filter events by category
 */
fun List<Event>.filterByCategory(category: EventCategory): List<Event> {
    return this.filter { it.category == category }
}

/**
 * Filter upcoming events
 */
fun List<Event>.filterUpcoming(): List<Event> {
    return this.filter { it.isUpcoming() }
}

/**
 * Filter past events
 */
fun List<Event>.filterPast(): List<Event> {
    return this.filter { it.hasPassed() }
}

/**
 * Sort events by date (nearest first)
 */
fun List<Event>.sortedByDateAscending(): List<Event> {
    return this.sortedBy { it.targetDate }
}

/**
 * Sort events by date (farthest first)
 */
fun List<Event>.sortedByDateDescending(): List<Event> {
    return this.sortedByDescending { it.targetDate }
}

/**
 * Sort events by title
 */
fun List<Event>.sortedByTitle(): List<Event> {
    return this.sortedBy { it.title.lowercase() }
}

/**
 * Group events by category
 */
fun List<Event>.groupedByCategory(): Map<EventCategory, List<Event>> {
    return this.groupBy { it.category }
}

/**
 * Group events by month
 */
fun List<Event>.groupedByMonth(): Map<String, List<Event>> {
    return this.groupBy {
        it.targetDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }
}

/**
 * Search events by query
 */
fun List<Event>.search(query: String): List<Event> {
    if (query.isBlank()) return this

    val lowerQuery = query.lowercase()
    return this.filter { event ->
        event.title.lowercase().contains(lowerQuery) ||
                event.notes.lowercase().contains(lowerQuery) ||
                event.category.displayName.lowercase().contains(lowerQuery)
    }
}

// ========================
// Context Extensions
// ========================

/**
 * Show a short toast
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show a long toast
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ========================
// Number Extensions
// ========================

/**
 * Format int as currency
 */
fun Int.toCurrency(): String {
    return "$${this / 100}.${(this % 100).toString().padStart(2, '0')}"
}

/**
 * Clamp number between min and max
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

// ========================
// Color Extensions
// ========================

/**
 * Convert Int color to Compose Color
 */
fun Int.toComposeColor(): Color = Color(this)

/**
 * Get contrasting color (black or white) for text
 */
fun Color.contrastingColor(): Color {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return if (luminance > 0.5) Color.Black else Color.White
}

/**
 * Darken color by percentage
 */
fun Color.darken(percentage: Float): Color {
    val factor = 1 - percentage.clamp(0f, 1f)
    return Color(
        red = (red * factor),
        green = (green * factor),
        blue = (blue * factor),
        alpha = alpha
    )
}

/**
 * Lighten color by percentage
 */
fun Color.lighten(percentage: Float): Color {
    val factor = percentage.clamp(0f, 1f)
    return Color(
        red = red + (1 - red) * factor,
        green = green + (1 - green) * factor,
        blue = blue + (1 - blue) * factor,
        alpha = alpha
    )
}
