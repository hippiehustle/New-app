package com.minicount.app.domain.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Data class representing countdown or count-up time information.
 *
 * @property years Years component of the countdown/up
 * @property months Months component (0-11)
 * @property days Days component (0-30)
 * @property hours Hours component (0-23)
 * @property minutes Minutes component (0-59)
 * @property seconds Seconds component (0-59)
 * @property isPast True if the target date is in the past (count-up mode)
 * @property totalDays Total number of days between now and target (absolute value)
 */
data class CountdownData(
    val years: Long = 0,
    val months: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isPast: Boolean = false,
    val totalDays: Long = 0
)

/**
 * Utility object for calculating and formatting countdown/count-up timers.
 *
 * Provides functions to:
 * - Calculate time differences between now and target dates
 * - Format countdown data in various display formats
 * - Calculate next occurrences for repeating events
 */
object CountdownCalculator {

    /**
     * Calculates the time difference between now and a target date.
     *
     * This function handles both future dates (countdown) and past dates (count-up).
     * It breaks down the difference into years, months, days, hours, minutes, and seconds
     * for accurate human-readable display.
     *
     * @param targetDate The target date to count down/up to
     * @return CountdownData containing all time components and metadata
     */
    fun calculate(targetDate: LocalDateTime): CountdownData {
        val now = LocalDateTime.now()
        val isPast = now.isAfter(targetDate)

        val startDate = if (isPast) targetDate else now
        val endDate = if (isPast) now else targetDate

        var years = ChronoUnit.YEARS.between(startDate, endDate)
        var tempDate = startDate.plusYears(years)

        var months = ChronoUnit.MONTHS.between(tempDate, endDate)
        tempDate = tempDate.plusMonths(months)

        var days = ChronoUnit.DAYS.between(tempDate, endDate)
        tempDate = tempDate.plusDays(days)

        val hours = ChronoUnit.HOURS.between(tempDate, endDate)
        tempDate = tempDate.plusHours(hours)

        val minutes = ChronoUnit.MINUTES.between(tempDate, endDate)
        tempDate = tempDate.plusMinutes(minutes)

        val seconds = ChronoUnit.SECONDS.between(tempDate, endDate)

        val totalDays = abs(ChronoUnit.DAYS.between(now, targetDate))

        return CountdownData(
            years = years,
            months = months,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isPast = isPast,
            totalDays = totalDays
        )
    }

    /**
     * Formats countdown data into a readable string.
     *
     * Examples:
     * - "2y 3mo 5d 12h 30m"
     * - "15d 8h 45m"
     * - "3h 20m 15s" (if showSeconds = true)
     *
     * @param data Countdown data to format
     * @param showSeconds Whether to include seconds in output (default: false)
     * @return Formatted countdown string
     */
    fun formatCountdown(data: CountdownData, showSeconds: Boolean = false): String {
        return buildString {
            if (data.years > 0) append("${data.years}y ")
            if (data.months > 0) append("${data.months}mo ")
            if (data.days > 0) append("${data.days}d ")
            if (data.hours > 0 || data.years == 0L) append("${data.hours}h ")
            if (data.minutes > 0 || (data.years == 0L && data.months == 0L)) {
                append("${data.minutes}m")
            }
            if (showSeconds && data.years == 0L && data.months == 0L && data.days == 0L) {
                append(" ${data.seconds}s")
            }
        }.trim()
    }

    /**
     * Formats countdown data into a compact two-component string.
     *
     * Shows only the two most significant time components:
     * - Years & months if years > 0
     * - Months & days if months > 0
     * - Days & hours if days > 0
     * - Hours & minutes if hours > 0
     * - Minutes & seconds otherwise
     *
     * Examples: "2y 3mo", "15d 8h", "45m 12s"
     *
     * @param data Countdown data to format
     * @return Compact formatted countdown string
     */
    fun formatShort(data: CountdownData): String {
        return when {
            data.years > 0 -> "${data.years}y ${data.months}mo"
            data.months > 0 -> "${data.months}mo ${data.days}d"
            data.days > 0 -> "${data.days}d ${data.hours}h"
            data.hours > 0 -> "${data.hours}h ${data.minutes}m"
            else -> "${data.minutes}m ${data.seconds}s"
        }
    }

    /**
     * Calculates the next occurrence of a repeating event.
     *
     * For repeating events, this function finds the next future occurrence
     * by adding the repeat interval to the base date until it's in the future.
     *
     * @param baseDate The original date of the event
     * @param interval The repeat interval (DAILY, WEEKLY, MONTHLY, YEARLY, or NONE)
     * @return The next future occurrence date, or baseDate if interval is NONE
     */
    fun getNextOccurrence(baseDate: LocalDateTime, interval: com.minicount.app.data.local.entity.RepeatInterval): LocalDateTime {
        val now = LocalDateTime.now()
        var nextDate = baseDate

        while (nextDate.isBefore(now)) {
            nextDate = when (interval) {
                com.minicount.app.data.local.entity.RepeatInterval.DAILY -> nextDate.plusDays(1)
                com.minicount.app.data.local.entity.RepeatInterval.WEEKLY -> nextDate.plusWeeks(1)
                com.minicount.app.data.local.entity.RepeatInterval.MONTHLY -> nextDate.plusMonths(1)
                com.minicount.app.data.local.entity.RepeatInterval.YEARLY -> nextDate.plusYears(1)
                com.minicount.app.data.local.entity.RepeatInterval.NONE -> return baseDate
            }
        }

        return nextDate
    }
}
