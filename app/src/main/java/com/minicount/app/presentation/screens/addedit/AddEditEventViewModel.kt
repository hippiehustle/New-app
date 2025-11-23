package com.minicount.app.presentation.screens.addedit

import android.net.Uri
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

    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _uiState = MutableStateFlow(AddEditEventUiState())
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()

    init {
        eventId?.let { id ->
            viewModelScope.launch {
                eventRepository.getEventByIdSync(id)?.let { event ->
                    _uiState.value = AddEditEventUiState(
                        title = event.title,
                        description = event.description,
                        targetDate = event.targetDate,
                        category = event.category,
                        photoUri = event.photoUri,
                        isRepeating = event.isRepeating,
                        repeatInterval = event.repeatInterval,
                        notificationEnabled = event.notificationEnabled,
                        notificationDaysBefore = event.notificationDaysBefore,
                        color = Color(event.color),
                        widgetStyle = event.widgetStyle,
                        isEditMode = true
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

    fun saveEvent(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title is required")
            return
        }

        viewModelScope.launch {
            val event = Event(
                id = eventId ?: 0,
                title = state.title,
                description = state.description,
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
            } else {
                eventRepository.insertEvent(event)
            }

            onSuccess()
        }
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
    val error: String? = null
)
