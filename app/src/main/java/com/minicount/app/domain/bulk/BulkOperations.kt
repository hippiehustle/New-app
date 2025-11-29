package com.minicount.app.domain.bulk

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.repository.EventRepository

class BulkOperations(
    private val eventRepository: EventRepository
) {
    suspend fun deleteMultiple(eventIds: List<Long>): Result<Int> {
        return try {
            var deletedCount = 0
            eventIds.forEach { id ->
                eventRepository.deleteEventById(id)
                deletedCount++
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategory(
        eventIds: List<Long>,
        newCategory: EventCategory
    ): Result<Int> {
        return try {
            var updatedCount = 0
            eventIds.forEach { id ->
                val event = eventRepository.getEventByIdSync(id)
                event?.let {
                    eventRepository.updateEvent(it.copy(category = newCategory))
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun togglePin(eventIds: List<Long>): Result<Int> {
        return try {
            var updatedCount = 0
            eventIds.forEach { id ->
                val event = eventRepository.getEventByIdSync(id)
                event?.let {
                    eventRepository.updateEvent(it.copy(isPinned = !it.isPinned))
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun enableNotifications(eventIds: List<Long>): Result<Int> {
        return try {
            var updatedCount = 0
            eventIds.forEach { id ->
                val event = eventRepository.getEventByIdSync(id)
                event?.let {
                    eventRepository.updateEvent(it.copy(notificationEnabled = true))
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun disableNotifications(eventIds: List<Long>): Result<Int> {
        return try {
            var updatedCount = 0
            eventIds.forEach { id ->
                val event = eventRepository.getEventByIdSync(id)
                event?.let {
                    eventRepository.updateEvent(it.copy(notificationEnabled = false))
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
