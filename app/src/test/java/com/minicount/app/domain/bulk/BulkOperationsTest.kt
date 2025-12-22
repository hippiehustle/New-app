package com.minicount.app.domain.bulk

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.repository.EventRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime

class BulkOperationsTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    private lateinit var bulkOperations: BulkOperations

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        bulkOperations = BulkOperations(eventRepository)
    }

    @Test
    fun `deleteMultiple successfully deletes all events`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L, 3L)
        val events = eventIds.map { createTestEvent(id = it) }

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(3, result.getOrNull())
        verify(eventRepository, times(3)).deleteEventById(anyLong())
    }

    @Test
    fun `deleteMultiple with empty list returns zero`() = runBlocking {
        // Given
        val eventIds = emptyList<Long>()

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(0, result.getOrNull())
        verify(eventRepository, never()).deleteEventById(anyLong())
    }

    @Test
    fun `deleteMultiple handles missing events gracefully`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L, 999L) // 999 doesn't exist
        `when`(eventRepository.getEventByIdSync(1L)).thenReturn(createTestEvent(id = 1))
        `when`(eventRepository.getEventByIdSync(2L)).thenReturn(createTestEvent(id = 2))
        `when`(eventRepository.getEventByIdSync(999L)).thenReturn(null)

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull()) // Only 2 deleted (1 and 2)
        verify(eventRepository, times(2)).deleteEventById(anyLong())
    }

    @Test
    fun `deleteMultiple returns failure on exception`() = runBlocking {
        // Given
        val eventIds = listOf(1L)
        `when`(eventRepository.getEventByIdSync(1L)).thenThrow(RuntimeException("Database error"))

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertTrue("Should contain error message",
            result.exceptionOrNull()?.message?.contains("Database error") == true)
    }

    @Test
    fun `updateCategoryForMultiple successfully updates all events`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L, 3L)
        val events = eventIds.map { createTestEvent(id = it, category = EventCategory.OTHER) }

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.updateCategoryForMultiple(eventIds, EventCategory.BIRTHDAY)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(3, result.getOrNull())
        verify(eventRepository, times(3)).updateEvent(argThat { event ->
            event.category == EventCategory.BIRTHDAY
        })
    }

    @Test
    fun `updateCategoryForMultiple with empty list returns zero`() = runBlocking {
        // Given
        val eventIds = emptyList<Long>()

        // When
        val result = bulkOperations.updateCategoryForMultiple(eventIds, EventCategory.BIRTHDAY)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(0, result.getOrNull())
        verify(eventRepository, never()).updateEvent(any())
    }

    @Test
    fun `togglePinForMultiple successfully toggles all events`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L)
        val events = listOf(
            createTestEvent(id = 1, isPinned = false),
            createTestEvent(id = 2, isPinned = true)
        )

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.togglePinForMultiple(eventIds, shouldPin = true)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(eventRepository, times(2)).updateEvent(argThat { event ->
            event.isPinned == true
        })
    }

    @Test
    fun `enableNotificationsForMultiple successfully enables notifications`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L)
        val events = eventIds.map { createTestEvent(id = it, notificationEnabled = false) }

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.enableNotificationsForMultiple(eventIds, enable = true)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(eventRepository, times(2)).updateEvent(argThat { event ->
            event.notificationEnabled == true
        })
    }

    @Test
    fun `disableNotificationsForMultiple successfully disables notifications`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L)
        val events = eventIds.map { createTestEvent(id = it, notificationEnabled = true) }

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.enableNotificationsForMultiple(eventIds, enable = false)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(eventRepository, times(2)).updateEvent(argThat { event ->
            event.notificationEnabled == false
        })
    }

    @Test
    fun `deleteAllInCategory successfully deletes matching events`() = runBlocking {
        // Given
        val allEvents = listOf(
            createTestEvent(id = 1, category = EventCategory.BIRTHDAY),
            createTestEvent(id = 2, category = EventCategory.WEDDING),
            createTestEvent(id = 3, category = EventCategory.BIRTHDAY)
        )

        // When
        val result = bulkOperations.deleteAllInCategory(allEvents, EventCategory.BIRTHDAY)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(eventRepository, times(2)).deleteEventById(anyLong())
    }

    @Test
    fun `deleteAllInCategory with no matching events returns zero`() = runBlocking {
        // Given
        val allEvents = listOf(
            createTestEvent(id = 1, category = EventCategory.WEDDING),
            createTestEvent(id = 2, category = EventCategory.ANNIVERSARY)
        )

        // When
        val result = bulkOperations.deleteAllInCategory(allEvents, EventCategory.BIRTHDAY)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(0, result.getOrNull())
        verify(eventRepository, never()).deleteEventById(anyLong())
    }

    @Test
    fun `bulk operations handle large datasets`() = runBlocking {
        // Given
        val eventIds = (1L..100L).toList()
        val events = eventIds.map { createTestEvent(id = it) }

        `when`(eventRepository.getEventByIdSync(anyLong())).thenAnswer { invocation ->
            val id = invocation.getArgument<Long>(0)
            events.find { it.id == id }
        }

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(100, result.getOrNull())
        verify(eventRepository, times(100)).deleteEventById(anyLong())
    }

    @Test
    fun `partial failure still processes successful operations`() = runBlocking {
        // Given
        val eventIds = listOf(1L, 2L, 3L)
        `when`(eventRepository.getEventByIdSync(1L)).thenReturn(createTestEvent(id = 1))
        `when`(eventRepository.getEventByIdSync(2L)).thenReturn(null) // Missing
        `when`(eventRepository.getEventByIdSync(3L)).thenReturn(createTestEvent(id = 3))

        // When
        val result = bulkOperations.deleteMultiple(eventIds)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(2, result.getOrNull()) // Only successfully deleted 2
    }

    private fun createTestEvent(
        id: Long,
        category: EventCategory = EventCategory.OTHER,
        isPinned: Boolean = false,
        notificationEnabled: Boolean = true
    ) = Event(
        id = id,
        title = "Test Event $id",
        notes = "",
        targetDate = LocalDateTime.now().plusDays(7),
        category = category,
        photoUri = null,
        isRepeating = false,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = notificationEnabled,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = isPinned
    )
}
