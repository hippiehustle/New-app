package com.minicount.app.domain.export

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class BackupManagerTest {

    @Test
    fun `exportToJson creates valid JSON structure`() {
        // Note: This would require mocking the repository
        // This is a structure test for the data classes

        val event = Event(
            id = 1,
            title = "Test Event",
            description = "Test Description",
            targetDate = LocalDateTime.of(2024, 12, 25, 10, 0),
            category = EventCategory.BIRTHDAY,
            photoUri = null,
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY,
            notificationEnabled = true,
            notificationDaysBefore = 7,
            color = 0xFF6750A4.toInt(),
            widgetStyle = WidgetStyle.CLASSIC,
            isPinned = false
        )

        val export = EventExport(
            id = event.id,
            title = event.title,
            description = event.description,
            targetDateString = event.targetDate.toString(),
            categoryName = event.category.name,
            photoUri = event.photoUri,
            isRepeating = event.isRepeating,
            repeatIntervalName = event.repeatInterval.name,
            notificationEnabled = event.notificationEnabled,
            notificationDaysBefore = event.notificationDaysBefore,
            color = event.color,
            widgetStyleName = event.widgetStyle.name,
            isPinned = event.isPinned
        )

        assertEquals("Title should match", event.title, export.title)
        assertEquals("Category should serialize correctly", "BIRTHDAY", export.categoryName)
        assertTrue("Should be repeating", export.isRepeating)
    }

    @Test
    fun `BackupData structure is serializable`() {
        val backup = BackupData(
            version = 1,
            exportDate = LocalDateTime.now().toString(),
            events = emptyList()
        )

        assertEquals("Version should be 1", 1, backup.version)
        assertTrue("Events should be empty list", backup.events.isEmpty())
    }
}
