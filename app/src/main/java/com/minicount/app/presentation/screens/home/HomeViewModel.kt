package com.minicount.app.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.common.UiState
import com.minicount.app.presentation.common.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    // UI State for events list
    val eventsState: StateFlow<UiState<List<Event>>> = eventRepository.getAllEvents()
        .map { events ->
            if (events.isEmpty()) {
                UiState.Empty("No events yet. Tap + to create your first event!")
            } else {
                UiState.Success(events)
            }
        }
        .catch { e ->
            Log.e(TAG, "Error loading events", e)
            emit(UiState.Error("Failed to load events: ${e.message}", e))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading
        )

    val eventCount: StateFlow<Int> = eventRepository.getEventCount()
        .catch { e ->
            Log.e(TAG, "Error loading event count", e)
            emit(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .catch { e ->
            Log.e(TAG, "Error loading premium status", e)
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
        .catch { e ->
            Log.e(TAG, "Error loading purchase state", e)
            emit(PurchaseState.Error("Billing unavailable"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PurchaseState.Loading)

    val canAddMoreEvents: StateFlow<Boolean> = combine(
        eventCount,
        isPremium
    ) { count, premium ->
        premium || count < BillingManager.FREE_WIDGET_LIMIT
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Action states for operations
    private val _deleteState = MutableStateFlow<ActionState>(ActionState.Idle)
    val deleteState: StateFlow<ActionState> = _deleteState.asStateFlow()

    private val _updateState = MutableStateFlow<ActionState>(ActionState.Idle)
    val updateState: StateFlow<ActionState> = _updateState.asStateFlow()

    /**
     * Delete an event with error handling
     */
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            try {
                _deleteState.value = ActionState.InProgress
                eventRepository.deleteEvent(event)
                _deleteState.value = ActionState.Success("Event deleted")
                Log.d(TAG, "Event deleted successfully: ${event.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete event: ${event.id}", e)
                _deleteState.value = ActionState.Error(
                    message = "Failed to delete event",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Toggle pin status of an event with error handling
     */
    fun togglePinEvent(event: Event) {
        viewModelScope.launch {
            try {
                _updateState.value = ActionState.InProgress
                eventRepository.updateEvent(event.copy(isPinned = !event.isPinned))
                _updateState.value = ActionState.Success(
                    if (event.isPinned) "Event unpinned" else "Event pinned"
                )
                Log.d(TAG, "Event pin toggled: ${event.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle pin for event: ${event.id}", e)
                _updateState.value = ActionState.Error(
                    message = "Failed to update event",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Retry the last failed delete operation
     */
    fun retryDelete(event: Event) {
        if (_deleteState.value is ActionState.Error) {
            deleteEvent(event)
        }
    }

    /**
     * Retry the last failed update operation
     */
    fun retryUpdate(event: Event) {
        if (_updateState.value is ActionState.Error) {
            togglePinEvent(event)
        }
    }

    /**
     * Clear action states
     */
    fun clearDeleteState() {
        _deleteState.value = ActionState.Idle
    }

    fun clearUpdateState() {
        _updateState.value = ActionState.Idle
    }
}
