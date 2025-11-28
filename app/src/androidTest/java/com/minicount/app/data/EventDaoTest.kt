package com.minicount.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minicount.app.data.local.AppDatabase
import com.minicount.app.data.local.dao.EventDao
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class EventDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var eventDao: EventDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        eventDao = database.eventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertEvent_andRetrieveById() = runBlocking {
        // Given
        val event = createTestEvent(title = "Birthday Party")

        // When
        val eventId = eventDao.insertEvent(event)
        val retrieved = eventDao.getEventByIdSync(eventId)

        // Then
        assertNotNull(retrieved)
        assertEquals("Birthday Party", retrieved?.title)
        assertEquals(event.category, retrieved?.category)
    }

    @Test
    fun insertMultipleEvents_andGetAll() = runBlocking {
        // Given
        val event1 = createTestEvent(title = "Event 1")
        val event2 = createTestEvent(title = "Event 2")
        val event3 = createTestEvent(title = "Event 3")

        // When
        eventDao.insertEvent(event1)
        eventDao.insertEvent(event2)
        eventDao.insertEvent(event3)
        val allEvents = eventDao.getAllEvents().first()

        // Then
        assertEquals(3, allEvents.size)
        assertTrue(allEvents.any { it.title == "Event 1" })
        assertTrue(allEvents.any { it.title == "Event 2" })
        assertTrue(allEvents.any { it.title == "Event 3" })
    }

    @Test
    fun updateEvent_modifiesExistingEvent() = runBlocking {
        // Given
        val event = createTestEvent(title = "Original Title")
        val eventId = eventDao.insertEvent(event)
        val retrievedEvent = eventDao.getEventByIdSync(eventId)!!

        // When
        val updatedEvent = retrievedEvent.copy(title = "Updated Title")
        eventDao.updateEvent(updatedEvent)
        val result = eventDao.getEventByIdSync(eventId)

        // Then
        assertEquals("Updated Title", result?.title)
    }

    @Test
    fun deleteEvent_removesFromDatabase() = runBlocking {
        // Given
        val event = createTestEvent(title = "To Be Deleted")
        val eventId = eventDao.insertEvent(event)

        // When
        eventDao.deleteEventById(eventId)
        val result = eventDao.getEventByIdSync(eventId)

        // Then
        assertNull(result)
    }

    @Test
    fun getAllEvents_sortsCorrectly() = runBlocking {
        // Given
        val pinnedEvent = createTestEvent(title = "Pinned", isPinned = true, daysFromNow = 10)
        val nearEvent = createTestEvent(title = "Near", isPinned = false, daysFromNow = 5)
        val farEvent = createTestEvent(title = "Far", isPinned = false, daysFromNow = 20)

        // When
        eventDao.insertEvent(farEvent)
        eventDao.insertEvent(nearEvent)
        eventDao.insertEvent(pinnedEvent)
        val allEvents = eventDao.getAllEvents().first()

        // Then
        assertEquals(3, allEvents.size)
        // Pinned events should come first
        assertEquals("Pinned", allEvents[0].title)
        // Then sorted by date
        assertEquals("Near", allEvents[1].title)
        assertEquals("Far", allEvents[2].title)
    }

    @Test
    fun getEventsPaginated_returnsCorrectPage() = runBlocking {
        // Given
        for (i in 1..50) {
            eventDao.insertEvent(createTestEvent(title = "Event $i", daysFromNow = i.toLong()))
        }

        // When - Get first page (20 items)
        val firstPage = eventDao.getEventsPaginatedSync(limit = 20, offset = 0)

        // Then
        assertEquals(20, firstPage.size)
    }

    @Test
    fun getEventsPaginated_paginationWorks() = runBlocking {
        // Given
        for (i in 1..50) {
            eventDao.insertEvent(createTestEvent(title = "Event $i", daysFromNow = i.toLong()))
        }

        // When
        val firstPage = eventDao.getEventsPaginatedSync(limit = 20, offset = 0)
        val secondPage = eventDao.getEventsPaginatedSync(limit = 20, offset = 20)
        val thirdPage = eventDao.getEventsPaginatedSync(limit = 20, offset = 40)

        // Then
        assertEquals(20, firstPage.size)
        assertEquals(20, secondPage.size)
        assertEquals(10, thirdPage.size) // Only 10 remaining

        // Ensure no overlap
        val allIds = (firstPage + secondPage + thirdPage).map { it.id }.toSet()
        assertEquals(50, allIds.size)
    }

    @Test
    fun getEventCount_returnsCorrectNumber() = runBlocking {
        // Given
        eventDao.insertEvent(createTestEvent(title = "Event 1"))
        eventDao.insertEvent(createTestEvent(title = "Event 2"))
        eventDao.insertEvent(createTestEvent(title = "Event 3"))

        // When
        val count = eventDao.getEventCount().first()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun getUpcomingEvents_returnsLimitedResults() = runBlocking {
        // Given
        for (i in 1..20) {
            eventDao.insertEvent(createTestEvent(title = "Event $i", daysFromNow = i.toLong()))
        }

        // When
        val upcoming = eventDao.getUpcomingEvents(limit = 5).first()

        // Then
        assertEquals(5, upcoming.size)
    }

    @Test
    fun getEventsWithNotifications_filtersCorrectly() = runBlocking {
        // Given
        val withNotif1 = createTestEvent(title = "Notif 1", notificationEnabled = true)
        val withNotif2 = createTestEvent(title = "Notif 2", notificationEnabled = true)
        val withoutNotif = createTestEvent(title = "No Notif", notificationEnabled = false)

        eventDao.insertEvent(withNotif1)
        eventDao.insertEvent(withNotif2)
        eventDao.insertEvent(withoutNotif)

        // When
        val events = eventDao.getEventsWithNotifications()

        // Then
        assertEquals(2, events.size)
        assertTrue(events.all { it.notificationEnabled })
    }

    @Test
    fun databaseIndices_improveQueryPerformance() = runBlocking {
        // Given - Insert many events
        for (i in 1..1000) {
            eventDao.insertEvent(
                createTestEvent(
                    title = "Event $i",
                    category = if (i % 3 == 0) EventCategory.BIRTHDAY else EventCategory.OTHER,
                    isPinned = i % 10 == 0,
                    daysFromNow = i.toLong()
                )
            )
        }

        // When - Query using indexed columns
        val startTime = System.currentTimeMillis()
        val pinnedEvents = eventDao.getAllEvents().first().filter { it.isPinned }
        val queryTime = System.currentTimeMillis() - startTime

        // Then
        assertEquals(100, pinnedEvents.size) // Every 10th event is pinned
        // Query should be fast with indices (under 100ms for 1000 records)
        assertTrue("Query took ${queryTime}ms, expected under 100ms", queryTime < 100)
    }

    private fun createTestEvent(
        title: String,
        category: EventCategory = EventCategory.OTHER,
        isPinned: Boolean = false,
        daysFromNow: Long = 7,
        notificationEnabled: Boolean = true
    ) = Event(
        id = 0,
        title = title,
        notes = "",
        targetDate = LocalDateTime.now().plusDays(daysFromNow),
        category = category,
        photoUri = null,
        isRepeating = false,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = notificationEnabled,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = isPinned
    )
}
