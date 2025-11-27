package com.minicount.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Tracks when events occur or pass, useful for repeating events and statistics
 */
@Entity(
    tableName = "event_history",
    indices = [
        androidx.room.Index(value = ["eventId"]),  // For querying history by event
        androidx.room.Index(value = ["occurredDate"]),  // For date range queries
        androidx.room.Index(value = ["recordedAt"])  // For recent history queries
    ]
)
data class EventHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val eventTitle: String,
    val eventCategory: EventCategory,
    val occurredDate: LocalDateTime,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val notes: String = ""
)

/**
 * Statistics data for an event based on history
 */
data class EventStatistics(
    val eventId: Long,
    val eventTitle: String,
    val totalOccurrences: Int,
    val firstOccurrence: LocalDateTime?,
    val lastOccurrence: LocalDateTime?,
    val averageDaysBetween: Double?
)
