package com.minicount.app.data.local.dao

import androidx.room.*
import com.minicount.app.data.local.entity.EventHistory
import com.minicount.app.data.local.entity.EventStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface EventHistoryDao {

    @Query("SELECT * FROM event_history ORDER BY occurredDate DESC")
    fun getAllHistory(): Flow<List<EventHistory>>

    @Query("SELECT * FROM event_history WHERE eventId = :eventId ORDER BY occurredDate DESC")
    fun getHistoryForEvent(eventId: Long): Flow<List<EventHistory>>

    @Query("""
        SELECT * FROM event_history
        WHERE occurredDate BETWEEN :startDate AND :endDate
        ORDER BY occurredDate DESC
    """)
    fun getHistoryInRange(startDate: Long, endDate: Long): Flow<List<EventHistory>>

    @Insert
    suspend fun insertHistory(history: EventHistory): Long

    @Update
    suspend fun updateHistory(history: EventHistory)

    @Delete
    suspend fun deleteHistory(history: EventHistory)

    @Query("DELETE FROM event_history WHERE eventId = :eventId")
    suspend fun deleteHistoryForEvent(eventId: Long)

    @Query("""
        SELECT
            eventId,
            eventTitle,
            COUNT(*) as totalOccurrences,
            MIN(occurredDate) as firstOccurrence,
            MAX(occurredDate) as lastOccurrence,
            0.0 as averageDaysBetween
        FROM event_history
        WHERE eventId = :eventId
        GROUP BY eventId
    """)
    suspend fun getStatisticsForEvent(eventId: Long): EventStatistics?

    @Query("SELECT COUNT(*) FROM event_history WHERE eventId = :eventId")
    suspend fun getOccurrenceCount(eventId: Long): Int
}
