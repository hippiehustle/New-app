package com.minicount.app.presentation.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.EventHistory
import com.minicount.app.data.repository.EventHistoryRepository
import com.minicount.app.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class EventHistoryViewModel @Inject constructor(
    private val eventHistoryRepository: EventHistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "EventHistoryViewModel"
    }

    val allHistoryState: StateFlow<UiState<List<EventHistory>>> = eventHistoryRepository.getAllHistory()
        .map { history ->
            if (history.isEmpty()) {
                UiState.Empty("No event history available")
            } else {
                UiState.Success(history)
            }
        }
        .catch { e ->
            Log.e(TAG, "Error loading event history", e)
            emit(UiState.Error("Failed to load history: ${e.message}", e))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading
        )

    /**
     * Get history for a specific event with error handling
     */
    fun getHistoryForEvent(eventId: Long): StateFlow<UiState<List<EventHistory>>> {
        return eventHistoryRepository.getHistoryForEvent(eventId)
            .map { history ->
                if (history.isEmpty()) {
                    UiState.Empty("No history for this event")
                } else {
                    UiState.Success(history)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error loading history for event $eventId", e)
                emit(UiState.Error("Failed to load event history: ${e.message}", e))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )
    }
}
