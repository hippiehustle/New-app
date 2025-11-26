package com.minicount.app.domain.recurrence

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.RepeatInterval
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles event recurrence logic and calculations
 */
@Singleton
class RecurrenceHandler @Inject constructor() {

    /**
     * Calculate the next occurrence of a repeating event
     */
    fun getNextOccurrence(event: Event, fromDate: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return null
        }

        var nextDate = event.targetDate

        // If the event's target date is in the future, return it
        if (nextDate.isAfter(fromDate)) {
            return nextDate
        }

        // Calculate the next occurrence based on repeat interval
        while (nextDate.isBefore(fromDate) || nextDate.isEqual(fromDate)) {
            nextDate = when (event.repeatInterval) {
                RepeatInterval.DAILY -> nextDate.plusDays(1)
                RepeatInterval.WEEKLY -> nextDate.plusWeeks(1)
                RepeatInterval.MONTHLY -> nextDate.plusMonths(1)
                RepeatInterval.YEARLY -> nextDate.plusYears(1)
                RepeatInterval.NONE -> return null
            }
        }

        return nextDate
    }

    /**
     * Get all occurrences of an event within a date range
     */
    fun getOccurrencesInRange(
        event: Event,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        maxOccurrences: Int = 100
    ): List<LocalDateTime> {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return if (event.targetDate in startDate..endDate) {
                listOf(event.targetDate)
            } else {
                emptyList()
            }
        }

        val occurrences = mutableListOf<LocalDateTime>()
        var currentDate = event.targetDate

        // Fast forward to start of range if needed
        while (currentDate.isBefore(startDate)) {
            currentDate = getNextOccurrenceDate(currentDate, event.repeatInterval)
        }

        // Collect occurrences within range
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (occurrences.size >= maxOccurrences) {
                break
            }
            occurrences.add(currentDate)
            currentDate = getNextOccurrenceDate(currentDate, event.repeatInterval)
        }

        return occurrences
    }

    /**
     * Calculate the number of occurrences between two dates
     */
    fun getOccurrenceCount(event: Event, startDate: LocalDateTime, endDate: LocalDateTime): Int {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return if (event.targetDate in startDate..endDate) 1 else 0
        }

        return when (event.repeatInterval) {
            RepeatInterval.DAILY -> ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
            RepeatInterval.WEEKLY -> (ChronoUnit.DAYS.between(startDate, endDate) / 7).toInt() + 1
            RepeatInterval.MONTHLY -> ChronoUnit.MONTHS.between(startDate, endDate).toInt() + 1
            RepeatInterval.YEARLY -> ChronoUnit.YEARS.between(startDate, endDate).toInt() + 1
            RepeatInterval.NONE -> 0
        }
    }

    /**
     * Check if an event occurs on a specific date
     */
    fun occursOnDate(event: Event, date: LocalDateTime): Boolean {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return event.targetDate.toLocalDate() == date.toLocalDate()
        }

        // For repeating events, check if date matches the pattern
        val daysBetween = ChronoUnit.DAYS.between(event.targetDate.toLocalDate(), date.toLocalDate())

        return when (event.repeatInterval) {
            RepeatInterval.DAILY -> daysBetween >= 0
            RepeatInterval.WEEKLY -> daysBetween >= 0 && daysBetween % 7 == 0L
            RepeatInterval.MONTHLY -> {
                daysBetween >= 0 && event.targetDate.dayOfMonth == date.dayOfMonth
            }
            RepeatInterval.YEARLY -> {
                daysBetween >= 0 &&
                        event.targetDate.dayOfMonth == date.dayOfMonth &&
                        event.targetDate.month == date.month
            }
            RepeatInterval.NONE -> false
        }
    }

    /**
     * Get the previous occurrence before a date
     */
    fun getPreviousOccurrence(event: Event, beforeDate: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return if (event.targetDate.isBefore(beforeDate)) event.targetDate else null
        }

        var previousDate = event.targetDate

        while (previousDate.isBefore(beforeDate)) {
            val nextDate = getNextOccurrenceDate(previousDate, event.repeatInterval)
            if (nextDate.isAfter(beforeDate) || nextDate.isEqual(beforeDate)) {
                return previousDate
            }
            previousDate = nextDate
        }

        return null
    }

    /**
     * Calculate the next occurrence date from a given date
     */
    private fun getNextOccurrenceDate(date: LocalDateTime, interval: RepeatInterval): LocalDateTime {
        return when (interval) {
            RepeatInterval.DAILY -> date.plusDays(1)
            RepeatInterval.WEEKLY -> date.plusWeeks(1)
            RepeatInterval.MONTHLY -> date.plusMonths(1)
            RepeatInterval.YEARLY -> date.plusYears(1)
            RepeatInterval.NONE -> date
        }
    }

    /**
     * Format recurrence rule as human-readable text
     */
    fun getRecurrenceDescription(event: Event): String? {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return null
        }

        return when (event.repeatInterval) {
            RepeatInterval.DAILY -> "Repeats daily"
            RepeatInterval.WEEKLY -> "Repeats weekly"
            RepeatInterval.MONTHLY -> "Repeats monthly on day ${event.targetDate.dayOfMonth}"
            RepeatInterval.YEARLY -> "Repeats yearly on ${event.targetDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${event.targetDate.dayOfMonth}"
            RepeatInterval.NONE -> null
        }
    }

    /**
     * Check if it's time to auto-advance a repeating event
     */
    fun shouldAdvanceEvent(event: Event): Boolean {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return false
        }

        val now = LocalDateTime.now()
        return event.targetDate.isBefore(now)
    }

    /**
     * Advance a repeating event to its next occurrence
     */
    fun advanceToNextOccurrence(event: Event): Event {
        if (!event.isRepeating || event.repeatInterval == RepeatInterval.NONE) {
            return event
        }

        val nextOccurrence = getNextOccurrence(event) ?: return event
        return event.copy(targetDate = nextOccurrence)
    }
}
