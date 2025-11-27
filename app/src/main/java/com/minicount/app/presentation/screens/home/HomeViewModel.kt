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
        private const val PAGE_SIZE = 20
    }

    // Pagination state
    private val _paginationEnabled = MutableStateFlow(false)
    val paginationEnabled: StateFlow<Boolean> = _paginationEnabled.asStateFlow()

    private val _currentOffset = MutableStateFlow(0)
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    // UI State for events list (non-paginated by default for backward compatibility)
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

    // Paginated events list
    private val _paginatedEvents = MutableStateFlow<List<Event>>(emptyList())
    val paginatedEventsState: StateFlow<UiState<List<Event>>> = _paginatedEvents
        .map { events ->
            if (events.isEmpty()) {
                UiState.Empty("No events yet. Tap + to create your first event!")
            } else {
                UiState.Success(events)
            }
        }
        .catch { e ->
            Log.e(TAG, "Error with paginated events", e)
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

    /**
     * Enable pagination mode - useful for large event lists
     * Call this early (e.g., in onCreate) to start with paginated loading
     */
    fun enablePagination() {
        if (!_paginationEnabled.value) {
            _paginationEnabled.value = true
            _currentOffset.value = 0
            _hasMorePages.value = true
            _paginatedEvents.value = emptyList()
            loadInitialPage()
            Log.d(TAG, "Pagination enabled with page size: $PAGE_SIZE")
        }
    }

    /**
     * Load initial page of events
     */
    private fun loadInitialPage() {
        viewModelScope.launch {
            try {
                val events = eventRepository.getEventsPaginatedSync(PAGE_SIZE, 0)
                _paginatedEvents.value = events
                _currentOffset.value = PAGE_SIZE
                _hasMorePages.value = events.size >= PAGE_SIZE
                Log.d(TAG, "Initial page loaded: ${events.size} events")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load initial page", e)
                _paginatedEvents.value = emptyList()
                _hasMorePages.value = false
            }
        }
    }

    /**
     * Load next page of events with error handling
     */
    fun loadMoreEvents() {
        if (!_paginationEnabled.value || !_hasMorePages.value || _loadingMore.value) {
            Log.d(TAG, "Load more skipped - enabled: ${_paginationEnabled.value}, hasMore: ${_hasMorePages.value}, loading: ${_loadingMore.value}")
            return
        }

        viewModelScope.launch {
            try {
                _loadingMore.value = true
                val currentOffset = _currentOffset.value
                val newEvents = eventRepository.getEventsPaginatedSync(PAGE_SIZE, currentOffset)

                if (newEvents.isEmpty()) {
                    _hasMorePages.value = false
                    Log.d(TAG, "No more events to load")
                } else {
                    _paginatedEvents.value = _paginatedEvents.value + newEvents
                    _currentOffset.value = currentOffset + newEvents.size
                    _hasMorePages.value = newEvents.size >= PAGE_SIZE
                    Log.d(TAG, "Loaded ${newEvents.size} more events, total: ${_paginatedEvents.value.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load more events", e)
                _hasMorePages.value = false
            } finally {
                _loadingMore.value = false
            }
        }
    }

    /**
     * Reset pagination and reload from start
     */
    fun resetPagination() {
        if (_paginationEnabled.value) {
            _currentOffset.value = 0
            _hasMorePages.value = true
            _paginatedEvents.value = emptyList()
            loadInitialPage()
            Log.d(TAG, "Pagination reset")
        }
    }
}
