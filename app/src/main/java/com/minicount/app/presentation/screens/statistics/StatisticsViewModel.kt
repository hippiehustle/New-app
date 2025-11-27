package com.minicount.app.presentation.screens.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.util.CountdownCalculator
import com.minicount.app.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StatisticsViewModel"
    }

    val statisticsState: StateFlow<UiState<EventStatistics>> = eventRepository.getAllEvents()
        .map { events ->
            try {
                if (events.isEmpty()) {
                    UiState.Empty("No events to show statistics for")
                } else {
                    UiState.Success(calculateStatistics(events))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating statistics", e)
                UiState.Error("Failed to calculate statistics: ${e.message}", e)
            }
        }
        .catch { e ->
            Log.e(TAG, "Error loading events for statistics", e)
            emit(UiState.Error("Failed to load statistics: ${e.message}", e))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading
        )

    /**
     * Calculate statistics from events with error handling
     */
    private fun calculateStatistics(events: List<Event>): EventStatistics {
        return try {
            val now = LocalDateTime.now()

            val upcoming = events.count { event ->
                try {
                    val targetDate = if (event.isRepeating) {
                        CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                    } else {
                        event.targetDate
                    }
                    targetDate.isAfter(now)
                } catch (e: Exception) {
                    Log.w(TAG, "Error calculating upcoming for event ${event.id}", e)
                    false
                }
            }

            val past = events.count { event ->
                try {
                    !event.isRepeating && event.targetDate.isBefore(now)
                } catch (e: Exception) {
                    Log.w(TAG, "Error calculating past for event ${event.id}", e)
                    false
                }
            }

            val repeating = events.count { it.isRepeating }

            val byCategory = events.groupBy { it.category }
                .mapValues { it.value.size }
                .toSortedMap(compareByDescending { it.displayName })

            val closestEvent = try {
                events
                    .filter { event ->
                        try {
                            val targetDate = if (event.isRepeating) {
                                CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                            } else {
                                event.targetDate
                            }
                            targetDate.isAfter(now)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error filtering closest event ${event.id}", e)
                            false
                        }
                    }
                    .minByOrNull { event ->
                        val targetDate = if (event.isRepeating) {
                            CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                        } else {
                            event.targetDate
                        }
                        ChronoUnit.DAYS.between(now, targetDate)
                    }?.let { event ->
                        val targetDate = if (event.isRepeating) {
                            CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                        } else {
                            event.targetDate
                        }
                        val days = ChronoUnit.DAYS.between(now, targetDate)
                        "${event.title} in $days day${if (days != 1L) "s" else ""}"
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error calculating closest event", e)
                null
            }

            EventStatistics(
                totalEvents = events.size,
                upcomingEvents = upcoming,
                pastEvents = past,
                repeatingEvents = repeating,
                eventsByCategory = byCategory,
                closestEvent = closestEvent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error calculating statistics", e)
            EventStatistics()
        }
    }
}

data class EventStatistics(
    val totalEvents: Int = 0,
    val upcomingEvents: Int = 0,
    val pastEvents: Int = 0,
    val repeatingEvents: Int = 0,
    val eventsByCategory: Map<EventCategory, Int> = emptyMap(),
    val closestEvent: String? = null
)
