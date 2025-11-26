package com.minicount.app.domain.search

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced search and filtering for events
 */
@Singleton
class EventSearchEngine @Inject constructor() {

    /**
     * Search filter criteria
     */
    data class SearchCriteria(
        val query: String? = null,
        val categories: Set<EventCategory>? = null,
        val startDate: LocalDateTime? = null,
        val endDate: LocalDateTime? = null,
        val onlyUpcoming: Boolean? = null,
        val onlyRepeating: Boolean? = null,
        val hasPhoto: Boolean? = null,
        val hasNotes: Boolean? = null,
        val repeatInterval: RepeatInterval? = null
    )

    /**
     * Sort options
     */
    enum class SortBy {
        DATE_ASCENDING,
        DATE_DESCENDING,
        TITLE_ASCENDING,
        TITLE_DESCENDING,
        CATEGORY,
        CREATED_DATE,
        RECENTLY_MODIFIED
    }

    /**
     * Search events with criteria
     */
    fun search(events: List<Event>, criteria: SearchCriteria): List<Event> {
        var results = events

        // Text search
        criteria.query?.let { query ->
            if (query.isNotBlank()) {
                results = searchByText(results, query)
            }
        }

        // Category filter
        criteria.categories?.let { categories ->
            if (categories.isNotEmpty()) {
                results = results.filter { it.category in categories }
            }
        }

        // Date range filter
        criteria.startDate?.let { start ->
            results = results.filter { it.targetDate.isAfter(start) || it.targetDate.isEqual(start) }
        }
        criteria.endDate?.let { end ->
            results = results.filter { it.targetDate.isBefore(end) || it.targetDate.isEqual(end) }
        }

        // Upcoming filter
        criteria.onlyUpcoming?.let { upcoming ->
            if (upcoming) {
                results = results.filter { it.targetDate.isAfter(LocalDateTime.now()) }
            }
        }

        // Repeating filter
        criteria.onlyRepeating?.let { repeating ->
            results = results.filter { it.isRepeating == repeating }
        }

        // Photo filter
        criteria.hasPhoto?.let { hasPhoto ->
            results = results.filter { (it.photoUri != null) == hasPhoto }
        }

        // Notes filter
        criteria.hasNotes?.let { hasNotes ->
            results = results.filter { it.notes.isNotBlank() == hasNotes }
        }

        // Repeat interval filter
        criteria.repeatInterval?.let { interval ->
            results = results.filter { it.repeatInterval == interval }
        }

        return results
    }

    /**
     * Search events by text query
     */
    private fun searchByText(events: List<Event>, query: String): List<Event> {
        val lowerQuery = query.lowercase().trim()

        if (lowerQuery.isEmpty()) return events

        return events.filter { event ->
            // Search in title
            event.title.lowercase().contains(lowerQuery) ||
                    // Search in notes
                    event.notes.lowercase().contains(lowerQuery) ||
                    // Search in category name
                    event.category.displayName.lowercase().contains(lowerQuery) ||
                    // Search in repeat interval
                    event.repeatInterval.name.lowercase().contains(lowerQuery)
        }.sortedByDescending { event ->
            // Calculate relevance score
            calculateRelevanceScore(event, lowerQuery)
        }
    }

    /**
     * Calculate relevance score for search results
     */
    private fun calculateRelevanceScore(event: Event, query: String): Int {
        var score = 0

        // Exact title match gets highest score
        if (event.title.lowercase() == query) {
            score += 1000
        }

        // Title starts with query
        if (event.title.lowercase().startsWith(query)) {
            score += 500
        }

        // Title contains query
        if (event.title.lowercase().contains(query)) {
            score += 100
        }

        // Category name match
        if (event.category.displayName.lowercase().contains(query)) {
            score += 50
        }

        // Notes contain query
        if (event.notes.lowercase().contains(query)) {
            score += 25
        }

        // Boost upcoming events slightly
        if (event.targetDate.isAfter(LocalDateTime.now())) {
            score += 10
        }

        return score
    }

    /**
     * Sort events by specified criteria
     */
    fun sort(events: List<Event>, sortBy: SortBy): List<Event> {
        return when (sortBy) {
            SortBy.DATE_ASCENDING -> events.sortedBy { it.targetDate }
            SortBy.DATE_DESCENDING -> events.sortedByDescending { it.targetDate }
            SortBy.TITLE_ASCENDING -> events.sortedBy { it.title.lowercase() }
            SortBy.TITLE_DESCENDING -> events.sortedByDescending { it.title.lowercase() }
            SortBy.CATEGORY -> events.sortedBy { it.category.ordinal }
            SortBy.CREATED_DATE -> events.sortedBy { it.id } // Assuming id is auto-increment
            SortBy.RECENTLY_MODIFIED -> events.sortedByDescending { it.id }
        }
    }

    /**
     * Get suggested categories based on query
     */
    fun suggestCategories(query: String): List<EventCategory> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()

        return EventCategory.entries.filter { category ->
            category.displayName.lowercase().contains(lowerQuery) ||
                    category.name.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Get quick filters
     */
    fun getQuickFilters(): List<QuickFilter> {
        return listOf(
            QuickFilter("All Events", SearchCriteria()),
            QuickFilter("Upcoming", SearchCriteria(onlyUpcoming = true)),
            QuickFilter("Today", SearchCriteria(
                startDate = LocalDateTime.now().toLocalDate().atStartOfDay(),
                endDate = LocalDateTime.now().toLocalDate().atTime(23, 59)
            )),
            QuickFilter("This Week", SearchCriteria(
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusWeeks(1)
            )),
            QuickFilter("This Month", SearchCriteria(
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusMonths(1)
            )),
            QuickFilter("Repeating", SearchCriteria(onlyRepeating = true)),
            QuickFilter("With Photos", SearchCriteria(hasPhoto = true))
        )
    }

    /**
     * Quick filter data class
     */
    data class QuickFilter(
        val name: String,
        val criteria: SearchCriteria
    )

    /**
     * Get statistics for search results
     */
    fun getSearchStatistics(events: List<Event>): SearchStatistics {
        val now = LocalDateTime.now()

        return SearchStatistics(
            total = events.size,
            upcoming = events.count { it.targetDate.isAfter(now) },
            past = events.count { it.targetDate.isBefore(now) },
            repeating = events.count { it.isRepeating },
            withPhotos = events.count { it.photoUri != null },
            withNotes = events.count { it.notes.isNotBlank() },
            categoryCounts = events.groupingBy { it.category }.eachCount()
        )
    }

    /**
     * Search statistics data class
     */
    data class SearchStatistics(
        val total: Int,
        val upcoming: Int,
        val past: Int,
        val repeating: Int,
        val withPhotos: Int,
        val withNotes: Int,
        val categoryCounts: Map<EventCategory, Int>
    )

    /**
     * Build search criteria from text query (smart parsing)
     */
    fun parseSmartQuery(query: String): SearchCriteria {
        val lowerQuery = query.lowercase().trim()

        var criteria = SearchCriteria(query = query)

        // Parse special keywords
        when {
            "upcoming" in lowerQuery -> criteria = criteria.copy(onlyUpcoming = true)
            "repeating" in lowerQuery -> criteria = criteria.copy(onlyRepeating = true)
            "photo" in lowerQuery || "image" in lowerQuery -> criteria = criteria.copy(hasPhoto = true)
        }

        // Parse date-related keywords
        when {
            "today" in lowerQuery -> {
                criteria = criteria.copy(
                    startDate = LocalDateTime.now().toLocalDate().atStartOfDay(),
                    endDate = LocalDateTime.now().toLocalDate().atTime(23, 59)
                )
            }
            "tomorrow" in lowerQuery -> {
                criteria = criteria.copy(
                    startDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay(),
                    endDate = LocalDateTime.now().plusDays(1).toLocalDate().atTime(23, 59)
                )
            }
            "this week" in lowerQuery -> {
                criteria = criteria.copy(
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now().plusWeeks(1)
                )
            }
        }

        // Parse category keywords
        EventCategory.entries.forEach { category ->
            if (category.displayName.lowercase() in lowerQuery) {
                criteria = criteria.copy(categories = setOf(category))
            }
        }

        return criteria
    }
}
