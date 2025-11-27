package com.minicount.app.presentation

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.common.UiState
import com.minicount.app.presentation.screens.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class HomeViewModelTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    @Mock
    private lateinit var billingManager: BillingManager

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))
        `when`(billingManager.purchaseState).thenReturn(MutableStateFlow(PurchaseState.Free))
        `when`(eventRepository.getEventCount()).thenReturn(flowOf(0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `eventsState should emit Loading initially`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)

        // Then
        val initialState = viewModel.eventsState.value
        assertTrue(initialState is UiState.Loading)
    }

    @Test
    fun `eventsState should emit Empty when no events exist`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.eventsState.value
        assertTrue(state is UiState.Empty)
        assertEquals("No events yet. Tap + to create your first event!", (state as UiState.Empty).message)
    }

    @Test
    fun `eventsState should emit Success when events exist`() = runTest {
        // Given
        val testEvents = listOf(
            createTestEvent(id = 1, title = "Birthday"),
            createTestEvent(id = 2, title = "Wedding")
        )
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(testEvents))

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.eventsState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("Birthday", state.data[0].title)
        assertEquals("Wedding", state.data[1].title)
    }

    @Test
    fun `deleteEvent should update deleteState to InProgress then Success`() = runTest {
        // Given
        val testEvent = createTestEvent(id = 1, title = "Test Event")
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(listOf(testEvent)))

        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // When
        viewModel.deleteEvent(testEvent)
        advanceUntilIdle()

        // Then
        verify(eventRepository).deleteEvent(testEvent)
        val deleteState = viewModel.deleteState.value
        assertTrue(deleteState is ActionState.Success)
        assertEquals("Event deleted", (deleteState as ActionState.Success).message)
    }

    @Test
    fun `togglePinEvent should toggle isPinned status`() = runTest {
        // Given
        val testEvent = createTestEvent(id = 1, title = "Test Event", isPinned = false)
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(listOf(testEvent)))

        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // When
        viewModel.togglePinEvent(testEvent)
        advanceUntilIdle()

        // Then
        verify(eventRepository).updateEvent(argThat { event ->
            event.id == testEvent.id && event.isPinned == true
        })
        val updateState = viewModel.updateState.value
        assertTrue(updateState is ActionState.Success)
        assertEquals("Event pinned", (updateState as ActionState.Success).message)
    }

    @Test
    fun `canAddMoreEvents should be true when premium`() = runTest {
        // Given
        `when`(preferencesManager.isPremium).thenReturn(flowOf(true))
        `when`(eventRepository.getEventCount()).thenReturn(flowOf(10))
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.canAddMoreEvents.value)
    }

    @Test
    fun `canAddMoreEvents should be false when free and at limit`() = runTest {
        // Given
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))
        `when`(eventRepository.getEventCount()).thenReturn(flowOf(BillingManager.FREE_WIDGET_LIMIT))
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.canAddMoreEvents.value)
    }

    @Test
    fun `enablePagination should load initial page`() = runTest {
        // Given
        val testEvents = (1..20).map { createTestEvent(id = it.toLong(), title = "Event $it") }
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))
        `when`(eventRepository.getEventsPaginatedSync(20, 0)).thenReturn(testEvents)

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        viewModel.enablePagination()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.paginationEnabled.value)
        assertTrue(viewModel.hasMorePages.value)
        val state = viewModel.paginatedEventsState.value
        assertTrue(state is UiState.Success)
        assertEquals(20, (state as UiState.Success).data.size)
    }

    @Test
    fun `loadMoreEvents should append to existing events`() = runTest {
        // Given
        val firstPage = (1..20).map { createTestEvent(id = it.toLong(), title = "Event $it") }
        val secondPage = (21..40).map { createTestEvent(id = it.toLong(), title = "Event $it") }
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(emptyList()))
        `when`(eventRepository.getEventsPaginatedSync(20, 0)).thenReturn(firstPage)
        `when`(eventRepository.getEventsPaginatedSync(20, 20)).thenReturn(secondPage)

        // When
        viewModel = HomeViewModel(eventRepository, preferencesManager, billingManager)
        viewModel.enablePagination()
        advanceUntilIdle()
        viewModel.loadMoreEvents()
        advanceUntilIdle()

        // Then
        val state = viewModel.paginatedEventsState.value
        assertTrue(state is UiState.Success)
        assertEquals(40, (state as UiState.Success).data.size)
    }

    private fun createTestEvent(
        id: Long,
        title: String,
        isPinned: Boolean = false
    ) = Event(
        id = id,
        title = title,
        notes = "",
        targetDate = LocalDateTime.now().plusDays(7),
        category = EventCategory.OTHER,
        photoUri = null,
        isRepeating = false,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = true,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = isPinned
    )
}
