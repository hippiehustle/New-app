package com.minicount.app.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.screens.addedit.AddEditEventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddEditEventViewModelTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddEditEventViewModel

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
    fun `uiState should initialize with default values for new event`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()

        // When
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `uiState should load existing event in edit mode`() = runTest {
        // Given
        val testEvent = createTestEvent(id = 1, title = "Birthday Party")
        savedStateHandle = SavedStateHandle(mapOf("eventId" to 1L))
        `when`(eventRepository.getEventByIdSync(1L)).thenReturn(testEvent)

        // When
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Birthday Party", state.title)
        assertTrue(state.isEditMode)
        assertFalse(state.isLoading)
        assertEquals(testEvent.category, state.category)
        assertEquals(testEvent.notes, state.description)
    }

    @Test
    fun `saveEvent should fail with validation error when title is blank`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        var successCalled = false
        val onSuccess = { successCalled = true }

        // When
        viewModel.saveEvent(onSuccess)
        advanceUntilIdle()

        // Then
        assertFalse(successCalled)
        val state = viewModel.uiState.value
        assertEquals("Title is required", state.error)
        verify(eventRepository, never()).insertEvent(any())
    }

    @Test
    fun `saveEvent should fail when title exceeds 100 characters`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        val longTitle = "a".repeat(101)
        viewModel.onTitleChange(longTitle)
        advanceUntilIdle()

        var successCalled = false
        val onSuccess = { successCalled = true }

        // When
        viewModel.saveEvent(onSuccess)
        advanceUntilIdle()

        // Then
        assertFalse(successCalled)
        assertEquals("Title must be less than 100 characters", viewModel.uiState.value.error)
    }

    @Test
    fun `saveEvent should fail when target date is more than 1 year in past`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.onTitleChange("Test Event")
        viewModel.onDateChange(LocalDateTime.now().minusYears(2))
        advanceUntilIdle()

        var successCalled = false
        val onSuccess = { successCalled = true }

        // When
        viewModel.saveEvent(onSuccess)
        advanceUntilIdle()

        // Then
        assertFalse(successCalled)
        assertEquals("Target date cannot be more than 1 year in the past", viewModel.uiState.value.error)
    }

    @Test
    fun `saveEvent should succeed and insert new event`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.onTitleChange("Birthday Party")
        viewModel.onDescriptionChange("Celebrate turning 30")
        viewModel.onCategoryChange(EventCategory.BIRTHDAY)
        advanceUntilIdle()

        `when`(eventRepository.insertEvent(any())).thenReturn(1L)

        var successCalled = false
        val onSuccess = { successCalled = true }

        // When
        viewModel.saveEvent(onSuccess)
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        verify(eventRepository).insertEvent(argThat { event ->
            event.title == "Birthday Party" &&
            event.notes == "Celebrate turning 30" &&
            event.category == EventCategory.BIRTHDAY
        })
        val saveState = viewModel.saveState.value
        assertTrue(saveState is ActionState.Success)
        assertEquals("Event created successfully", (saveState as ActionState.Success).message)
    }

    @Test
    fun `saveEvent should succeed and update existing event`() = runTest {
        // Given
        val testEvent = createTestEvent(id = 1, title = "Old Title")
        savedStateHandle = SavedStateHandle(mapOf("eventId" to 1L))
        `when`(eventRepository.getEventByIdSync(1L)).thenReturn(testEvent)

        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.onTitleChange("New Title")
        advanceUntilIdle()

        var successCalled = false
        val onSuccess = { successCalled = true }

        // When
        viewModel.saveEvent(onSuccess)
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        verify(eventRepository).updateEvent(argThat { event ->
            event.id == 1L && event.title == "New Title"
        })
        val saveState = viewModel.saveState.value
        assertTrue(saveState is ActionState.Success)
        assertEquals("Event updated successfully", (saveState as ActionState.Success).message)
    }

    @Test
    fun `onTitleChange should update uiState title`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onTitleChange("New Title")
        advanceUntilIdle()

        // Then
        assertEquals("New Title", viewModel.uiState.value.title)
    }

    @Test
    fun `onCategoryChange should update uiState category`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onCategoryChange(EventCategory.WEDDING)
        advanceUntilIdle()

        // Then
        assertEquals(EventCategory.WEDDING, viewModel.uiState.value.category)
    }

    @Test
    fun `onRepeatingChange should update uiState isRepeating`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onRepeatingChange(true)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isRepeating)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle()
        viewModel = AddEditEventViewModel(eventRepository, savedStateHandle)
        advanceUntilIdle()

        // Trigger validation error
        viewModel.saveEvent {}
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    private fun createTestEvent(
        id: Long,
        title: String
    ) = Event(
        id = id,
        title = title,
        notes = "Test notes",
        targetDate = LocalDateTime.now().plusDays(30),
        category = EventCategory.BIRTHDAY,
        photoUri = null,
        isRepeating = false,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = true,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = false
    )
}
