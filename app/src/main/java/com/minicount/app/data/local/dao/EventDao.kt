package com.minicount.app.data.local.dao

import androidx.room.*
import com.minicount.app.data.local.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY isPinned DESC, targetDate ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY isPinned DESC, targetDate ASC LIMIT :limit OFFSET :offset")
    fun getEventsPaginated(limit: Int, offset: Int): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY isPinned DESC, targetDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getEventsPaginatedSync(limit: Int, offset: Int): List<Event>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventByIdSync(eventId: Long): Event?

    @Query("SELECT * FROM events ORDER BY targetDate ASC LIMIT :limit")
    fun getUpcomingEvents(limit: Int = 10): Flow<List<Event>>

    @Query("SELECT COUNT(*) FROM events")
    fun getEventCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("SELECT * FROM events WHERE notificationEnabled = 1")
    suspend fun getEventsWithNotifications(): List<Event>
}
