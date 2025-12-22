package com.minicount.app.domain.search

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class EventSearchEngineTest {

    @Test
    fun `filterEvents with empty filter returns all events`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Birthday"),
            createTestEvent(title = "Wedding"),
            createTestEvent(title = "Anniversary")
        )
        val filter = EventFilter()

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(3, result.size)
    }

    @Test
    fun `filterEvents with search query filters by title`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Birthday Party"),
            createTestEvent(title = "Wedding Ceremony"),
            createTestEvent(title = "Anniversary Dinner")
        )
        val filter = EventFilter(searchQuery = "Birthday")

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("Birthday Party", result[0].title)
    }

    @Test
    fun `filterEvents with search query is case insensitive`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Birthday Party"),
            createTestEvent(title = "Wedding Ceremony")
        )
        val filter = EventFilter(searchQuery = "birthday")

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("Birthday Party", result[0].title)
    }

    @Test
    fun `filterEvents by category filters correctly`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Birthday 1", category = EventCategory.BIRTHDAY),
            createTestEvent(title = "Wedding 1", category = EventCategory.WEDDING),
            createTestEvent(title = "Birthday 2", category = EventCategory.BIRTHDAY)
        )
        val filter = EventFilter(category = EventCategory.BIRTHDAY)

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == EventCategory.BIRTHDAY })
    }

    @Test
    fun `filterEvents by isPinned filters correctly`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Pinned 1", isPinned = true),
            createTestEvent(title = "Not Pinned", isPinned = false),
            createTestEvent(title = "Pinned 2", isPinned = true)
        )
        val filter = EventFilter(isPinned = true)

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isPinned })
    }

    @Test
    fun `filterEvents by isRepeating filters correctly`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Repeating 1", isRepeating = true),
            createTestEvent(title = "One Time", isRepeating = false),
            createTestEvent(title = "Repeating 2", isRepeating = true)
        )
        val filter = EventFilter(isRepeating = true)

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isRepeating })
    }

    @Test
    fun `filterEvents by hasNotification filters correctly`() {
        // Given
        val events = listOf(
            createTestEvent(title = "With Notif 1", notificationEnabled = true),
            createTestEvent(title = "Without Notif", notificationEnabled = false),
            createTestEvent(title = "With Notif 2", notificationEnabled = true)
        )
        val filter = EventFilter(hasNotification = true)

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.notificationEnabled })
    }

    @Test
    fun `filterEvents by date range filters correctly`() {
        // Given
        val today = LocalDate.now()
        val events = listOf(
            createTestEvent(title = "Soon", targetDate = today.plusDays(5).atStartOfDay()),
            createTestEvent(title = "Later", targetDate = today.plusDays(15).atStartOfDay()),
            createTestEvent(title = "Much Later", targetDate = today.plusDays(25).atStartOfDay())
        )
        val filter = EventFilter(
            dateRangeStart = today.atStartOfDay(),
            dateRangeEnd = today.plusDays(10).atStartOfDay()
        )

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("Soon", result[0].title)
    }

    @Test
    fun `filterEvents with multiple criteria applies all filters`() {
        // Given
        val events = listOf(
            createTestEvent(
                title = "Birthday Party",
                category = EventCategory.BIRTHDAY,
                isPinned = true,
                notificationEnabled = true
            ),
            createTestEvent(
                title = "Birthday Dinner",
                category = EventCategory.BIRTHDAY,
                isPinned = false,
                notificationEnabled = true
            ),
            createTestEvent(
                title = "Wedding",
                category = EventCategory.WEDDING,
                isPinned = true,
                notificationEnabled = true
            )
        )
        val filter = EventFilter(
            searchQuery = "Birthday",
            category = EventCategory.BIRTHDAY,
            isPinned = true
        )

        // When
        val result = EventSearchEngine.filterEvents(events, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("Birthday Party", result[0].title)
    }

    @Test
    fun `sortEvents by PINNED_FIRST puts pinned events first`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Normal 1", isPinned = false, daysFromNow = 5),
            createTestEvent(title = "Pinned 1", isPinned = true, daysFromNow = 10),
            createTestEvent(title = "Normal 2", isPinned = false, daysFromNow = 3),
            createTestEvent(title = "Pinned 2", isPinned = true, daysFromNow = 7)
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.PINNED_FIRST)

        // Then
        assertEquals("Pinned 2", result[0].title) // Pinned + earlier date
        assertEquals("Pinned 1", result[1].title) // Pinned + later date
        assertEquals("Normal 2", result[2].title) // Not pinned, earlier date
        assertEquals("Normal 1", result[3].title) // Not pinned, later date
    }

    @Test
    fun `sortEvents by DATE_NEAREST sorts by closest date`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Far", daysFromNow = 30),
            createTestEvent(title = "Near", daysFromNow = 5),
            createTestEvent(title = "Middle", daysFromNow = 15)
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.DATE_NEAREST)

        // Then
        assertEquals("Near", result[0].title)
        assertEquals("Middle", result[1].title)
        assertEquals("Far", result[2].title)
    }

    @Test
    fun `sortEvents by DATE_FARTHEST sorts by furthest date`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Near", daysFromNow = 5),
            createTestEvent(title = "Far", daysFromNow = 30),
            createTestEvent(title = "Middle", daysFromNow = 15)
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.DATE_FARTHEST)

        // Then
        assertEquals("Far", result[0].title)
        assertEquals("Middle", result[1].title)
        assertEquals("Near", result[2].title)
    }

    @Test
    fun `sortEvents by NAME_AZ sorts alphabetically`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Zebra"),
            createTestEvent(title = "Apple"),
            createTestEvent(title = "Mango")
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.NAME_AZ)

        // Then
        assertEquals("Apple", result[0].title)
        assertEquals("Mango", result[1].title)
        assertEquals("Zebra", result[2].title)
    }

    @Test
    fun `sortEvents by NAME_ZA sorts reverse alphabetically`() {
        // Given
        val events = listOf(
            createTestEvent(title = "Apple"),
            createTestEvent(title = "Zebra"),
            createTestEvent(title = "Mango")
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.NAME_ZA)

        // Then
        assertEquals("Zebra", result[0].title)
        assertEquals("Mango", result[1].title)
        assertEquals("Apple", result[2].title)
    }

    @Test
    fun `sortEvents by CREATED_NEWEST sorts by creation date`() {
        // Given
        val now = LocalDateTime.now()
        val events = listOf(
            createTestEvent(title = "Old", createdAt = now.minusDays(10)),
            createTestEvent(title = "New", createdAt = now),
            createTestEvent(title = "Middle", createdAt = now.minusDays(5))
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.CREATED_NEWEST)

        // Then
        assertEquals("New", result[0].title)
        assertEquals("Middle", result[1].title)
        assertEquals("Old", result[2].title)
    }

    @Test
    fun `sortEvents by CREATED_OLDEST sorts by creation date reversed`() {
        // Given
        val now = LocalDateTime.now()
        val events = listOf(
            createTestEvent(title = "New", createdAt = now),
            createTestEvent(title = "Old", createdAt = now.minusDays(10)),
            createTestEvent(title = "Middle", createdAt = now.minusDays(5))
        )

        // When
        val result = EventSearchEngine.sortEvents(events, EventSortBy.CREATED_OLDEST)

        // Then
        assertEquals("Old", result[0].title)
        assertEquals("Middle", result[1].title)
        assertEquals("New", result[2].title)
    }

    @Test
    fun `EventFilter hasActiveFilters returns true when filters applied`() {
        // Given
        val filter = EventFilter(
            category = EventCategory.BIRTHDAY,
            isPinned = true
        )

        // When/Then
        assertTrue(filter.hasActiveFilters())
    }

    @Test
    fun `EventFilter hasActiveFilters returns false when no filters`() {
        // Given
        val filter = EventFilter()

        // When/Then
        assertFalse(filter.hasActiveFilters())
    }

    @Test
    fun `EventFilter activeFilterCount returns correct count`() {
        // Given
        val filter = EventFilter(
            category = EventCategory.BIRTHDAY,
            isPinned = true,
            hasNotification = true
        )

        // When
        val count = filter.activeFilterCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `EventFilter copy creates independent instance`() {
        // Given
        val original = EventFilter(category = EventCategory.BIRTHDAY)

        // When
        val copy = original.copy(isPinned = true)

        // Then
        assertEquals(EventCategory.BIRTHDAY, copy.category)
        assertTrue(copy.isPinned!!)
        assertNull(original.isPinned)
    }

    private fun createTestEvent(
        title: String,
        category: EventCategory = EventCategory.OTHER,
        isPinned: Boolean = false,
        isRepeating: Boolean = false,
        notificationEnabled: Boolean = true,
        targetDate: LocalDateTime = LocalDateTime.now().plusDays(7),
        daysFromNow: Long = 7,
        createdAt: LocalDateTime = LocalDateTime.now()
    ) = Event(
        id = 0,
        title = title,
        notes = "",
        targetDate = if (targetDate == LocalDateTime.now().plusDays(7)) {
            LocalDateTime.now().plusDays(daysFromNow)
        } else {
            targetDate
        },
        category = category,
        photoUri = null,
        isRepeating = isRepeating,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = notificationEnabled,
        notificationDaysBefore = 1,
        createdAt = createdAt,
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = isPinned
    )
}
