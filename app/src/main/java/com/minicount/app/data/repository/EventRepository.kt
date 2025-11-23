package com.minicount.app.data.repository

import com.minicount.app.data.local.dao.EventDao
import com.minicount.app.data.local.entity.Event
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    fun getEventById(eventId: Long): Flow<Event?> = eventDao.getEventById(eventId)

    suspend fun getEventByIdSync(eventId: Long): Event? = eventDao.getEventByIdSync(eventId)

    fun getUpcomingEvents(limit: Int = 10): Flow<List<Event>> = eventDao.getUpcomingEvents(limit)

    fun getEventCount(): Flow<Int> = eventDao.getEventCount()

    suspend fun getEventCountSync(): Int = eventDao.getEventCountSync()

    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)

    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    suspend fun deleteEventById(eventId: Long) = eventDao.deleteEventById(eventId)

    suspend fun getEventsWithNotifications(): List<Event> = eventDao.getEventsWithNotifications()
}
