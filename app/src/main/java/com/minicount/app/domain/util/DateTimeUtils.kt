package com.minicount.app.domain.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for date and time operations
 */
@Singleton
class DateTimeUtils @Inject constructor() {

    companion object {
        // Common formatters
        val DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val DATE_FORMAT_MEDIUM = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        val TIME_FORMAT_12H = DateTimeFormatter.ofPattern("hh:mm a")
        val TIME_FORMAT_24H = DateTimeFormatter.ofPattern("HH:mm")

        // ISO formatter
        val ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    /**
     * Get current date and time
     */
    fun now(): LocalDateTime = LocalDateTime.now()

    /**
     * Get current date
     */
    fun today(): LocalDate = LocalDate.now()

    /**
     * Get tomorrow's date
     */
    fun tomorrow(): LocalDate = LocalDate.now().plusDays(1)

    /**
     * Get yesterday's date
     */
    fun yesterday(): LocalDate = LocalDate.now().minusDays(1)

    /**
     * Get start of current week (Monday)
     */
    fun startOfWeek(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * Get end of current week (Sunday)
     */
    fun endOfWeek(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    /**
     * Get start of current month
     */
    fun startOfMonth(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    }

    /**
     * Get end of current month
     */
    fun endOfMonth(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
    }

    /**
     * Get start of current year
     */
    fun startOfYear(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
    }

    /**
     * Get end of current year
     */
    fun endOfYear(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfYear())
    }

    /**
     * Calculate days between two dates
     */
    fun daysBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    /**
     * Calculate hours between two date-times
     */
    fun hoursBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.HOURS.between(start, end)
    }

    /**
     * Calculate minutes between two date-times
     */
    fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(start, end)
    }

    /**
     * Calculate seconds between two date-times
     */
    fun secondsBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    /**
     * Check if a date is within a range
     */
    fun isInRange(date: LocalDateTime, start: LocalDateTime, end: LocalDateTime): Boolean {
        return !date.isBefore(start) && !date.isAfter(end)
    }

    /**
     * Get the day of week name
     */
    fun getDayOfWeekName(date: LocalDate, style: TextStyle = TextStyle.FULL): String {
        return date.dayOfWeek.getDisplayName(style, Locale.getDefault())
    }

    /**
     * Get the month name
     */
    fun getMonthName(date: LocalDate, style: TextStyle = TextStyle.FULL): String {
        return date.month.getDisplayName(style, Locale.getDefault())
    }

    /**
     * Get all dates in a month
     */
    fun getDatesInMonth(year: Int, month: Int): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth())

        var currentDate = firstDay
        while (!currentDate.isAfter(lastDay)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }

    /**
     * Get all dates in a date range
     */
    fun getDatesBetween(start: LocalDate, end: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }

    /**
     * Get calendar grid for a month (includes days from previous/next month)
     */
    fun getCalendarGrid(year: Int, month: Int): List<LocalDate> {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth())

        // Get the Monday of the week containing the first day
        val gridStart = firstDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Get the Sunday of the week containing the last day
        val gridEnd = lastDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return getDatesBetween(gridStart, gridEnd)
    }

    /**
     * Format duration in human-readable format
     */
    fun formatDuration(seconds: Long): String {
        val duration = Duration.ofSeconds(seconds)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val secs = duration.seconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (secs > 0 || isEmpty()) append("${secs}s")
        }.trim()
    }

    /**
     * Get relative time description
     */
    fun getRelativeTimeString(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(now, dateTime)

        return when {
            duration.isNegative -> getPastTimeString(duration.abs())
            else -> getFutureTimeString(duration)
        }
    }

    private fun getPastTimeString(duration: Duration): String {
        val seconds = duration.seconds
        return when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60} minutes ago"
            seconds < 86400 -> "${seconds / 3600} hours ago"
            seconds < 604800 -> "${seconds / 86400} days ago"
            seconds < 2592000 -> "${seconds / 604800} weeks ago"
            seconds < 31536000 -> "${seconds / 2592000} months ago"
            else -> "${seconds / 31536000} years ago"
        }
    }

    private fun getFutureTimeString(duration: Duration): String {
        val seconds = duration.seconds
        return when {
            seconds < 60 -> "In a moment"
            seconds < 3600 -> "In ${seconds / 60} minutes"
            seconds < 86400 -> "In ${seconds / 3600} hours"
            seconds < 604800 -> "In ${seconds / 86400} days"
            seconds < 2592000 -> "In ${seconds / 604800} weeks"
            seconds < 31536000 -> "In ${seconds / 2592000} months"
            else -> "In ${seconds / 31536000} years"
        }
    }

    /**
     * Check if a year is a leap year
     */
    fun isLeapYear(year: Int): Boolean {
        return Year.of(year).isLeap
    }

    /**
     * Get number of days in a month
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return YearMonth.of(year, month).lengthOfMonth()
    }

    /**
     * Get the week number of the year
     */
    fun getWeekOfYear(date: LocalDate): Int {
        return date.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfYear())
    }

    /**
     * Convert LocalDateTime to epoch millis
     */
    fun toEpochMilli(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Convert epoch millis to LocalDateTime
     */
    fun fromEpochMilli(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
    }

    /**
     * Parse date string with pattern
     */
    fun parseDate(dateString: String, pattern: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse datetime string with pattern
     */
    fun parseDateTime(dateTimeString: String, pattern: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get age from birthdate
     */
    fun getAge(birthDate: LocalDate): Int {
        return Period.between(birthDate, LocalDate.now()).years
    }

    /**
     * Get next birthday date
     */
    fun getNextBirthday(birthDate: LocalDate): LocalDate {
        val today = LocalDate.now()
        var nextBirthday = birthDate.withYear(today.year)

        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1)
        }

        return nextBirthday
    }

    /**
     * Check if two dates are on the same day
     */
    fun isSameDay(date1: LocalDateTime, date2: LocalDateTime): Boolean {
        return date1.toLocalDate() == date2.toLocalDate()
    }

    /**
     * Round time to nearest interval (e.g., 15 minutes)
     */
    fun roundToNearestMinutes(dateTime: LocalDateTime, minutes: Int): LocalDateTime {
        val remainder = dateTime.minute % minutes
        return if (remainder < minutes / 2) {
            dateTime.minusMinutes(remainder.toLong())
        } else {
            dateTime.plusMinutes((minutes - remainder).toLong())
        }.truncatedTo(ChronoUnit.MINUTES)
    }
}
