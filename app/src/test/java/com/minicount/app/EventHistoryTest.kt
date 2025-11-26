package com.minicount.app

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.EventHistory
import com.minicount.app.data.local.entity.RepeatInterval
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class EventHistoryTest {

    @Test
    fun `EventHistory creation with default values`() {
        val now = LocalDateTime.now()
        val history = EventHistory(
            eventId = 1L,
            eventTitle = "Test Event",
            eventCategory = EventCategory.BIRTHDAY,
            occurredDate = now
        )

        assertEquals(1L, history.eventId)
        assertEquals("Test Event", history.eventTitle)
        assertEquals(EventCategory.BIRTHDAY, history.eventCategory)
        assertEquals(now, history.occurredDate)
        assertEquals("", history.notes)
        assertNotNull(history.recordedAt)
    }

    @Test
    fun `EventHistory with notes`() {
        val history = EventHistory(
            eventId = 1L,
            eventTitle = "Test Event",
            eventCategory = EventCategory.WEDDING,
            occurredDate = LocalDateTime.now(),
            notes = "This is a test note"
        )

        assertEquals("This is a test note", history.notes)
    }

    @Test
    fun `Event categories have correct properties`() {
        assertEquals("🎂", EventCategory.BIRTHDAY.icon)
        assertEquals("Birthday", EventCategory.BIRTHDAY.displayName)

        assertEquals("💍", EventCategory.WEDDING.icon)
        assertEquals("Wedding", EventCategory.WEDDING.displayName)

        assertEquals("🎓", EventCategory.GRADUATION.icon)
        assertEquals("Graduation", EventCategory.GRADUATION.displayName)
    }

    @Test
    fun `RepeatInterval values are correct`() {
        assertEquals(RepeatInterval.NONE, RepeatInterval.valueOf("NONE"))
        assertEquals(RepeatInterval.DAILY, RepeatInterval.valueOf("DAILY"))
        assertEquals(RepeatInterval.WEEKLY, RepeatInterval.valueOf("WEEKLY"))
        assertEquals(RepeatInterval.MONTHLY, RepeatInterval.valueOf("MONTHLY"))
        assertEquals(RepeatInterval.YEARLY, RepeatInterval.valueOf("YEARLY"))
    }

    @Test
    fun `Event creation with all parameters`() {
        val targetDate = LocalDateTime.of(2025, 12, 25, 10, 0)
        val event = Event(
            id = 1L,
            title = "Christmas",
            targetDate = targetDate,
            category = EventCategory.HOLIDAY,
            photoUri = "content://photo.jpg",
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY,
            color = 0xFFFF0000.toInt(),
            widgetStyle = "classic",
            notes = "Family gathering"
        )

        assertEquals("Christmas", event.title)
        assertEquals(targetDate, event.targetDate)
        assertEquals(EventCategory.HOLIDAY, event.category)
        assertEquals("content://photo.jpg", event.photoUri)
        assertTrue(event.isRepeating)
        assertEquals(RepeatInterval.YEARLY, event.repeatInterval)
        assertEquals("Family gathering", event.notes)
    }

    @Test
    fun `Event with no photo and no repeat`() {
        val event = Event(
            title = "Simple Event",
            targetDate = LocalDateTime.now(),
            category = EventCategory.OTHER
        )

        assertNull(event.photoUri)
        assertFalse(event.isRepeating)
        assertEquals(RepeatInterval.NONE, event.repeatInterval)
    }
}
