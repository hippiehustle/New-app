package com.minicount.app.presentation.screens.widget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WidgetPreviewViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    companion object {
        private const val TAG = "WidgetPreviewViewModel"
    }

    /**
     * Get event for widget preview with error handling
     */
    fun getEvent(eventId: Long): StateFlow<UiState<Event>> {
        return eventRepository.getEventById(eventId)
            .map { event ->
                if (event == null) {
                    UiState.Empty("Event not found")
                } else {
                    UiState.Success(event)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error loading event for preview: $eventId", e)
                emit(UiState.Error("Failed to load event: ${e.message}", e))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )
    }
}
