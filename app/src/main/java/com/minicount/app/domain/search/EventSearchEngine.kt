package com.minicount.app.domain.search

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import java.time.LocalDateTime

data class EventFilter(
    val searchQuery: String? = null,
    val categories: Set<EventCategory>? = null,
    val isPinned: Boolean? = null,
    val isRepeating: Boolean? = null,
    val dateRange: DateRange? = null,
    val hasPhoto: Boolean? = null
) {
    fun hasActiveFilters(): Boolean {
        return searchQuery?.isNotBlank() == true ||
                categories != null ||
                isPinned != null ||
                isRepeating != null ||
                dateRange != null ||
                hasPhoto != null
    }

    fun activeFilterCount(): Int {
        var count = 0
        if (searchQuery?.isNotBlank() == true) count++
        if (categories != null) count++
        if (isPinned != null) count++
        if (isRepeating != null) count++
        if (dateRange != null) count++
        if (hasPhoto != null) count++
        return count
    }
}

sealed class DateRange {
    object Today : DateRange()
    object ThisWeek : DateRange()
    object ThisMonth : DateRange()
    object ThisYear : DateRange()
    object Past : DateRange()
    object Future : DateRange()
    data class Custom(val start: LocalDateTime, val end: LocalDateTime) : DateRange()
}

object EventSearchEngine {
    fun filterEvents(events: List<Event>, filter: EventFilter): List<Event> {
        var filtered = events
        filter.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                filtered = filtered.filter { event ->
                    event.title.contains(query, ignoreCase = true) ||
                            event.notes.contains(query, ignoreCase = true)
                }
            }
        }
        filter.categories?.let { categories ->
            if (categories.isNotEmpty()) {
                filtered = filtered.filter { event -> event.category in categories }
            }
        }
        filter.isPinned?.let { pinned -> filtered = filtered.filter { it.isPinned == pinned } }
        filter.isRepeating?.let { repeating -> filtered = filtered.filter { it.isRepeating == repeating } }
        filter.dateRange?.let { range -> filtered = filtered.filter { isInDateRange(it.targetDate, range) } }
        filter.hasPhoto?.let { hasPhoto -> filtered = filtered.filter { (it.photoUri != null) == hasPhoto } }
        return filtered
    }

    fun sortEvents(events: List<Event>, sortBy: EventSortBy): List<Event> {
        return when (sortBy) {
            EventSortBy.DATE_ASC -> events.sortedBy { it.targetDate }
            EventSortBy.DATE_DESC -> events.sortedByDescending { it.targetDate }
            EventSortBy.TITLE_ASC -> events.sortedBy { it.title.lowercase() }
            EventSortBy.TITLE_DESC -> events.sortedByDescending { it.title.lowercase() }
            EventSortBy.PINNED_FIRST -> events.sortedWith(compareByDescending<Event> { it.isPinned }.thenBy { it.targetDate })
        }
    }

    private fun isInDateRange(date: LocalDateTime, range: DateRange): Boolean {
        val now = LocalDateTime.now()
        return when (range) {
            DateRange.Today -> date.toLocalDate() == now.toLocalDate()
            DateRange.ThisWeek -> {
                val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                date.isAfter(weekStart) && date.isBefore(weekStart.plusDays(7))
            }
            DateRange.ThisMonth -> date.year == now.year && date.monthValue == now.monthValue
            DateRange.ThisYear -> date.year == now.year
            DateRange.Past -> date.isBefore(now)
            DateRange.Future -> date.isAfter(now)
            is DateRange.Custom -> date.isAfter(range.start) && date.isBefore(range.end)
        }
    }
}

enum class EventSortBy {
    DATE_ASC, DATE_DESC, TITLE_ASC, TITLE_DESC, PINNED_FIRST
}
