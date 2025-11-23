package com.minicount.app.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.util.CountdownCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    val statistics: StateFlow<EventStatistics> = eventRepository.getAllEvents()
        .map { events -> calculateStatistics(events) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventStatistics())

    private fun calculateStatistics(events: List<Event>): EventStatistics {
        val now = LocalDateTime.now()

        val upcoming = events.count { event ->
            val targetDate = if (event.isRepeating) {
                CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
            } else {
                event.targetDate
            }
            targetDate.isAfter(now)
        }

        val past = events.count { event ->
            !event.isRepeating && event.targetDate.isBefore(now)
        }

        val repeating = events.count { it.isRepeating }

        val byCategory = events.groupBy { it.category }
            .mapValues { it.value.size }
            .toSortedMap(compareByDescending { it.displayName })

        val closestEvent = events
            .filter { event ->
                val targetDate = if (event.isRepeating) {
                    CountdownCalculator.getNextOccurrence(event.targetDate, event.repeatInterval)
                } else {
                    event.targetDate
                }
                targetDate.isAfter(now)
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

        return EventStatistics(
            totalEvents = events.size,
            upcomingEvents = upcoming,
            pastEvents = past,
            repeatingEvents = repeating,
            eventsByCategory = byCategory,
            closestEvent = closestEvent
        )
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
