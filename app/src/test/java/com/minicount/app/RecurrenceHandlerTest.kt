package com.minicount.app

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.domain.recurrence.RecurrenceHandler
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class RecurrenceHandlerTest {

    private lateinit var recurrenceHandler: RecurrenceHandler

    @Before
    fun setup() {
        recurrenceHandler = RecurrenceHandler()
    }

    @Test
    fun `non-repeating event returns null for next occurrence`() {
        val event = Event(
            title = "Non-repeating Event",
            targetDate = LocalDateTime.now().plusDays(5),
            category = EventCategory.BIRTHDAY,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE
        )

        val nextOccurrence = recurrenceHandler.getNextOccurrence(event)
        assertNull(nextOccurrence)
    }

    @Test
    fun `daily repeating event calculates next occurrence correctly`() {
        val baseDate = LocalDateTime.of(2025, 1, 1, 10, 0)
        val event = Event(
            title = "Daily Event",
            targetDate = baseDate,
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.DAILY
        )

        val fromDate = LocalDateTime.of(2025, 1, 5, 12, 0)
        val nextOccurrence = recurrenceHandler.getNextOccurrence(event, fromDate)

        assertNotNull(nextOccurrence)
        assertEquals(LocalDateTime.of(2025, 1, 6, 10, 0), nextOccurrence)
    }

    @Test
    fun `weekly repeating event calculates next occurrence correctly`() {
        val baseDate = LocalDateTime.of(2025, 1, 1, 10, 0)
        val event = Event(
            title = "Weekly Event",
            targetDate = baseDate,
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.WEEKLY
        )

        val fromDate = LocalDateTime.of(2025, 1, 10, 12, 0)
        val nextOccurrence = recurrenceHandler.getNextOccurrence(event, fromDate)

        assertNotNull(nextOccurrence)
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), nextOccurrence)
    }

    @Test
    fun `monthly repeating event calculates next occurrence correctly`() {
        val baseDate = LocalDateTime.of(2025, 1, 15, 10, 0)
        val event = Event(
            title = "Monthly Event",
            targetDate = baseDate,
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.MONTHLY
        )

        val fromDate = LocalDateTime.of(2025, 3, 20, 12, 0)
        val nextOccurrence = recurrenceHandler.getNextOccurrence(event, fromDate)

        assertNotNull(nextOccurrence)
        assertEquals(LocalDateTime.of(2025, 4, 15, 10, 0), nextOccurrence)
    }

    @Test
    fun `yearly repeating event calculates next occurrence correctly`() {
        val baseDate = LocalDateTime.of(2025, 6, 15, 10, 0)
        val event = Event(
            title = "Yearly Event",
            targetDate = baseDate,
            category = EventCategory.BIRTHDAY,
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY
        )

        val fromDate = LocalDateTime.of(2025, 12, 1, 12, 0)
        val nextOccurrence = recurrenceHandler.getNextOccurrence(event, fromDate)

        assertNotNull(nextOccurrence)
        assertEquals(LocalDateTime.of(2026, 6, 15, 10, 0), nextOccurrence)
    }

    @Test
    fun `occursOnDate returns true for correct date`() {
        val baseDate = LocalDateTime.of(2025, 1, 1, 10, 0)
        val event = Event(
            title = "Daily Event",
            targetDate = baseDate,
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.DAILY
        )

        val checkDate = LocalDateTime.of(2025, 1, 5, 14, 30)
        assertTrue(recurrenceHandler.occursOnDate(event, checkDate))
    }

    @Test
    fun `occursOnDate returns false for incorrect date`() {
        val baseDate = LocalDateTime.of(2025, 1, 1, 10, 0)
        val event = Event(
            title = "Weekly Event",
            targetDate = baseDate,
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.WEEKLY
        )

        val checkDate = LocalDateTime.of(2025, 1, 5, 14, 30)
        assertFalse(recurrenceHandler.occursOnDate(event, checkDate))
    }

    @Test
    fun `getRecurrenceDescription returns correct text`() {
        val dailyEvent = Event(
            title = "Daily",
            targetDate = LocalDateTime.now(),
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.DAILY
        )

        assertEquals("Repeats daily", recurrenceHandler.getRecurrenceDescription(dailyEvent))
    }

    @Test
    fun `shouldAdvanceEvent returns true for past event`() {
        val pastEvent = Event(
            title = "Past Event",
            targetDate = LocalDateTime.now().minusDays(1),
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.DAILY
        )

        assertTrue(recurrenceHandler.shouldAdvanceEvent(pastEvent))
    }

    @Test
    fun `shouldAdvanceEvent returns false for future event`() {
        val futureEvent = Event(
            title = "Future Event",
            targetDate = LocalDateTime.now().plusDays(1),
            category = EventCategory.OTHER,
            isRepeating = true,
            repeatInterval = RepeatInterval.DAILY
        )

        assertFalse(recurrenceHandler.shouldAdvanceEvent(futureEvent))
    }
}
