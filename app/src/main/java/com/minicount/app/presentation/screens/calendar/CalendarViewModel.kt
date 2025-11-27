package com.minicount.app.presentation.screens.calendar

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
class CalendarViewModel @Inject constructor(
    eventRepository: EventRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarViewModel"
    }

    val eventsState: StateFlow<UiState<List<Event>>> = eventRepository.getAllEvents()
        .map { events ->
            if (events.isEmpty()) {
                UiState.Empty("No events to display on calendar")
            } else {
                UiState.Success(events)
            }
        }
        .catch { e ->
            Log.e(TAG, "Error loading calendar events", e)
            emit(UiState.Error("Failed to load calendar: ${e.message}", e))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading
        )
}
