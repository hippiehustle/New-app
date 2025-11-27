package com.minicount.app.data.repository

import com.minicount.app.data.local.dao.EventDao
import com.minicount.app.data.local.entity.Event
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing countdown events.
 *
 * Provides a clean API for accessing and manipulating event data.
 * All database operations are performed through this repository to maintain
 * separation of concerns and enable easier testing.
 *
 * @property eventDao Data access object for event operations
 */
@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {

    /**
     * Retrieves all events as a Flow, sorted by pinned status and target date.
     *
     * @return Flow emitting list of all events, updated automatically when data changes
     */
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    /**
     * Retrieves a paginated list of events as a Flow.
     * Useful for displaying large event lists efficiently.
     *
     * @param limit Maximum number of events to retrieve
     * @param offset Number of events to skip (for pagination)
     * @return Flow emitting paginated list of events
     */
    fun getEventsPaginated(limit: Int, offset: Int): Flow<List<Event>> =
        eventDao.getEventsPaginated(limit, offset)

    /**
     * Retrieves a paginated list of events synchronously.
     * Use this for one-time queries where reactive updates aren't needed.
     *
     * @param limit Maximum number of events to retrieve
     * @param offset Number of events to skip (for pagination)
     * @return List of events for the requested page
     */
    suspend fun getEventsPaginatedSync(limit: Int, offset: Int): List<Event> =
        eventDao.getEventsPaginatedSync(limit, offset)

    /**
     * Retrieves a single event by ID as a Flow.
     *
     * @param eventId Unique identifier of the event
     * @return Flow emitting the event or null if not found
     */
    fun getEventById(eventId: Long): Flow<Event?> = eventDao.getEventById(eventId)

    /**
     * Retrieves a single event by ID synchronously.
     *
     * @param eventId Unique identifier of the event
     * @return The event or null if not found
     */
    suspend fun getEventByIdSync(eventId: Long): Event? = eventDao.getEventByIdSync(eventId)

    /**
     * Retrieves upcoming events sorted by target date.
     *
     * @param limit Maximum number of events to retrieve (default: 10)
     * @return Flow emitting list of upcoming events
     */
    fun getUpcomingEvents(limit: Int = 10): Flow<List<Event>> = eventDao.getUpcomingEvents(limit)

    /**
     * Gets the total count of all events as a Flow.
     *
     * @return Flow emitting the current event count
     */
    fun getEventCount(): Flow<Int> = eventDao.getEventCount()

    /**
     * Gets the total count of all events synchronously.
     *
     * @return Current event count
     */
    suspend fun getEventCountSync(): Int = eventDao.getEventCountSync()

    /**
     * Inserts a new event into the database.
     *
     * @param event Event to insert
     * @return ID of the newly inserted event
     */
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)

    /**
     * Updates an existing event in the database.
     *
     * @param event Event to update (must have existing ID)
     */
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    /**
     * Deletes an event from the database.
     *
     * @param event Event to delete
     */
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    /**
     * Deletes an event by its ID.
     *
     * @param eventId ID of the event to delete
     */
    suspend fun deleteEventById(eventId: Long) = eventDao.deleteEventById(eventId)

    /**
     * Retrieves all events that have notifications enabled.
     * Used by the notification worker to schedule reminders.
     *
     * @return List of events with notifications enabled
     */
    suspend fun getEventsWithNotifications(): List<Event> = eventDao.getEventsWithNotifications()
}
