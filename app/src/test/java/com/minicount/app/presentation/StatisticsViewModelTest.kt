package com.minicount.app.presentation

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.presentation.common.UiState
import com.minicount.app.presentation.screens.statistics.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    private lateinit var viewModel: StatisticsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `statisticsState should emit Loading initially`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = StatisticsViewModel(eventRepository)

        // Then
        val initialState = viewModel.statisticsState.value
        assertTrue(initialState is UiState.Loading)
    }

    @Test
    fun `statisticsState should emit Empty when no events exist`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Empty)
        assertEquals("No events to show statistics for", (state as UiState.Empty).message)
    }

    @Test
    fun `statisticsState should calculate correct statistics for mixed events`() = runTest {
        // Given
        val events = listOf(
            // Past events
            createTestEvent(id = 1, title = "Past Event 1", daysFromNow = -30),
            createTestEvent(id = 2, title = "Past Event 2", daysFromNow = -15),

            // Upcoming events
            createTestEvent(id = 3, title = "Upcoming Event 1", daysFromNow = 7),
            createTestEvent(id = 4, title = "Upcoming Event 2", daysFromNow = 30),

            // Repeating event (always upcoming)
            createTestEvent(id = 5, title = "Repeating Event", daysFromNow = -5, isRepeating = true),

            // Events in different categories
            createTestEvent(id = 6, title = "Birthday", daysFromNow = 10, category = EventCategory.BIRTHDAY),
            createTestEvent(id = 7, title = "Wedding", daysFromNow = 20, category = EventCategory.WEDDING)
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(events))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Success)

        val stats = (state as UiState.Success).data
        assertEquals(7, stats.totalEvents)
        assertEquals(5, stats.upcomingEvents) // 3 future + 1 repeating + 1 repeating that's future
        assertEquals(2, stats.pastEvents) // 2 non-repeating past events
        assertEquals(1, stats.repeatingEvents) // 1 repeating event
        assertNotNull(stats.closestEvent)
    }

    @Test
    fun `statisticsState should group events by category correctly`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(id = 1, title = "Birthday 1", daysFromNow = 10, category = EventCategory.BIRTHDAY),
            createTestEvent(id = 2, title = "Birthday 2", daysFromNow = 20, category = EventCategory.BIRTHDAY),
            createTestEvent(id = 3, title = "Birthday 3", daysFromNow = 30, category = EventCategory.BIRTHDAY),
            createTestEvent(id = 4, title = "Wedding 1", daysFromNow = 15, category = EventCategory.WEDDING),
            createTestEvent(id = 5, title = "Wedding 2", daysFromNow = 25, category = EventCategory.WEDDING),
            createTestEvent(id = 6, title = "Other", daysFromNow = 5, category = EventCategory.OTHER)
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(events))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Success)

        val stats = (state as UiState.Success).data
        val byCategory = stats.eventsByCategory

        assertEquals(3, byCategory[EventCategory.BIRTHDAY])
        assertEquals(2, byCategory[EventCategory.WEDDING])
        assertEquals(1, byCategory[EventCategory.OTHER])
    }

    @Test
    fun `statisticsState should identify closest upcoming event`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(id = 1, title = "Far Event", daysFromNow = 100),
            createTestEvent(id = 2, title = "Closest Event", daysFromNow = 3),
            createTestEvent(id = 3, title = "Middle Event", daysFromNow = 50)
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(events))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Success)

        val stats = (state as UiState.Success).data
        assertNotNull(stats.closestEvent)
        assertTrue(stats.closestEvent!!.contains("Closest Event"))
        assertTrue(stats.closestEvent!!.contains("3 days"))
    }

    @Test
    fun `statisticsState should handle only past events`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(id = 1, title = "Past Event 1", daysFromNow = -10),
            createTestEvent(id = 2, title = "Past Event 2", daysFromNow = -20),
            createTestEvent(id = 3, title = "Past Event 3", daysFromNow = -30)
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(events))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Success)

        val stats = (state as UiState.Success).data
        assertEquals(3, stats.totalEvents)
        assertEquals(0, stats.upcomingEvents)
        assertEquals(3, stats.pastEvents)
        assertNull(stats.closestEvent) // No upcoming events
    }

    @Test
    fun `statisticsState should handle only repeating events`() = runTest {
        // Given
        val events = listOf(
            createTestEvent(id = 1, title = "Repeating 1", daysFromNow = -30, isRepeating = true),
            createTestEvent(id = 2, title = "Repeating 2", daysFromNow = -60, isRepeating = true)
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(events))

        // When
        viewModel = StatisticsViewModel(eventRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.statisticsState.value
        assertTrue(state is UiState.Success)

        val stats = (state as UiState.Success).data
        assertEquals(2, stats.totalEvents)
        assertEquals(2, stats.upcomingEvents) // Both repeating, so always upcoming
        assertEquals(0, stats.pastEvents) // Repeating events don't count as past
        assertEquals(2, stats.repeatingEvents)
    }

    private fun createTestEvent(
        id: Long,
        title: String,
        daysFromNow: Long,
        isRepeating: Boolean = false,
        category: EventCategory = EventCategory.OTHER
    ) = Event(
        id = id,
        title = title,
        notes = "",
        targetDate = LocalDateTime.now().plusDays(daysFromNow),
        category = category,
        photoUri = null,
        isRepeating = isRepeating,
        repeatInterval = if (isRepeating) RepeatInterval.YEARLY else RepeatInterval.NONE,
        notificationEnabled = true,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = false
    )
}
