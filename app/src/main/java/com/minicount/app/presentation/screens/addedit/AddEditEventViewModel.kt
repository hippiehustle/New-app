package com.minicount.app.presentation.screens.addedit

import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.presentation.common.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "AddEditEventViewModel"
    }

    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _uiState = MutableStateFlow(AddEditEventUiState())
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<ActionState>(ActionState.Idle)
    val saveState: StateFlow<ActionState> = _saveState.asStateFlow()

    init {
        loadEvent()
    }

    /**
     * Load event data for editing with error handling
     */
    private fun loadEvent() {
        eventId?.let { id ->
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                    val event = eventRepository.getEventByIdSync(id)
                    if (event != null) {
                        _uiState.value = AddEditEventUiState(
                            title = event.title,
                            description = event.notes,
                            targetDate = event.targetDate,
                            category = event.category,
                            photoUri = event.photoUri,
                            isRepeating = event.isRepeating,
                            repeatInterval = event.repeatInterval,
                            notificationEnabled = event.notificationEnabled,
                            notificationDaysBefore = event.notificationDaysBefore,
                            color = Color(event.color),
                            widgetStyle = event.widgetStyle,
                            isEditMode = true,
                            isLoading = false
                        )
                        Log.d(TAG, "Event loaded successfully: $id")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Event not found"
                        )
                        Log.w(TAG, "Event not found: $id")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load event: $id", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load event: ${e.message}"
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onDateChange(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(targetDate = date)
    }

    fun onCategoryChange(category: EventCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onPhotoUriChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(photoUri = uri?.toString())
    }

    fun onRepeatingChange(isRepeating: Boolean) {
        _uiState.value = _uiState.value.copy(isRepeating = isRepeating)
    }

    fun onRepeatIntervalChange(interval: RepeatInterval) {
        _uiState.value = _uiState.value.copy(repeatInterval = interval)
    }

    fun onNotificationEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)
    }

    fun onNotificationDaysBeforeChange(days: Int) {
        _uiState.value = _uiState.value.copy(notificationDaysBefore = days)
    }

    fun onColorChange(color: Color) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun onWidgetStyleChange(style: WidgetStyle) {
        _uiState.value = _uiState.value.copy(widgetStyle = style)
    }

    /**
     * Save event with comprehensive error handling and validation
     */
    fun saveEvent(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title is required")
            return
        }

        if (state.title.length > 100) {
            _uiState.value = state.copy(error = "Title must be less than 100 characters")
            return
        }

        if (state.targetDate.isBefore(LocalDateTime.now().minusYears(1))) {
            _uiState.value = state.copy(error = "Target date cannot be more than 1 year in the past")
            return
        }

        if (state.notificationDaysBefore < 0 || state.notificationDaysBefore > 365) {
            _uiState.value = state.copy(error = "Notification days must be between 0 and 365")
            return
        }

        viewModelScope.launch {
            try {
                _saveState.value = ActionState.InProgress
                _uiState.value = state.copy(error = null)

                val event = Event(
                    id = eventId ?: 0,
                    title = state.title.trim(),
                    notes = state.description.trim(),
                    targetDate = state.targetDate,
                    category = state.category,
                    photoUri = state.photoUri,
                    isRepeating = state.isRepeating,
                    repeatInterval = state.repeatInterval,
                    notificationEnabled = state.notificationEnabled,
                    notificationDaysBefore = state.notificationDaysBefore,
                    color = state.color.toArgb(),
                    widgetStyle = state.widgetStyle
                )

                if (eventId != null) {
                    eventRepository.updateEvent(event)
                    _saveState.value = ActionState.Success("Event updated successfully")
                    Log.d(TAG, "Event updated: $eventId")
                } else {
                    val newId = eventRepository.insertEvent(event)
                    _saveState.value = ActionState.Success("Event created successfully")
                    Log.d(TAG, "Event created: $newId")
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save event", e)
                _saveState.value = ActionState.Error(
                    message = "Failed to save event: ${e.message}",
                    exception = e,
                    canRetry = true
                )
                _uiState.value = state.copy(error = "Failed to save event. Please try again.")
            }
        }
    }

    /**
     * Retry save operation
     */
    fun retrySave(onSuccess: () -> Unit) {
        if (_saveState.value is ActionState.Error) {
            saveEvent(onSuccess)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear save state
     */
    fun clearSaveState() {
        _saveState.value = ActionState.Idle
    }
}

data class AddEditEventUiState(
    val title: String = "",
    val description: String = "",
    val targetDate: LocalDateTime = LocalDateTime.now().plusDays(1),
    val category: EventCategory = EventCategory.OTHER,
    val photoUri: String? = null,
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.YEARLY,
    val notificationEnabled: Boolean = true,
    val notificationDaysBefore: Int = 1,
    val color: Color = Color(0xFF6750A4),
    val widgetStyle: WidgetStyle = WidgetStyle.CLASSIC,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
