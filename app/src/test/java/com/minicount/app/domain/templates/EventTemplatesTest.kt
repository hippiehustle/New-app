package com.minicount.app.domain.templates

import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import org.junit.Assert.*
import org.junit.Test

class EventTemplatesTest {

    @Test
    fun `ALL_TEMPLATES contains expected templates`() {
        // When
        val templates = EventTemplates.ALL_TEMPLATES

        // Then
        assertTrue("Should have multiple templates", templates.size >= 8)
        assertTrue("Should contain birthday template",
            templates.any { it.name == "Birthday" })
        assertTrue("Should contain wedding template",
            templates.any { it.name == "Wedding" })
        assertTrue("Should contain anniversary template",
            templates.any { it.name == "Anniversary" })
    }

    @Test
    fun `BIRTHDAY template has correct properties`() {
        // When
        val template = EventTemplates.BIRTHDAY

        // Then
        assertEquals("Birthday", template.name)
        assertEquals("🎂", template.emoji)
        assertEquals(EventCategory.BIRTHDAY, template.category)
        assertEquals(RepeatInterval.YEARLY, template.repeatInterval)
        assertEquals(7, template.notificationDaysBefore)
        assertNotNull(template.description)
    }

    @Test
    fun `WEDDING template has correct properties`() {
        // When
        val template = EventTemplates.WEDDING

        // Then
        assertEquals("Wedding", template.name)
        assertEquals("💒", template.emoji)
        assertEquals(EventCategory.WEDDING, template.category)
        assertEquals(RepeatInterval.NONE, template.repeatInterval)
        assertEquals(30, template.notificationDaysBefore)
    }

    @Test
    fun `ANNIVERSARY template has correct properties`() {
        // When
        val template = EventTemplates.ANNIVERSARY

        // Then
        assertEquals("Anniversary", template.name)
        assertEquals("💝", template.emoji)
        assertEquals(EventCategory.ANNIVERSARY, template.category)
        assertEquals(RepeatInterval.YEARLY, template.repeatInterval)
        assertEquals(7, template.notificationDaysBefore)
    }

    @Test
    fun `VACATION template has correct properties`() {
        // When
        val template = EventTemplates.VACATION

        // Then
        assertEquals("Vacation", template.name)
        assertEquals("✈️", template.emoji)
        assertEquals(EventCategory.VACATION, template.category)
        assertEquals(RepeatInterval.NONE, template.repeatInterval)
        assertEquals(14, template.notificationDaysBefore)
    }

    @Test
    fun `GRADUATION template has correct properties`() {
        // When
        val template = EventTemplates.GRADUATION

        // Then
        assertEquals("Graduation", template.name)
        assertEquals("🎓", template.emoji)
        assertEquals(EventCategory.GRADUATION, template.category)
        assertEquals(RepeatInterval.NONE, template.repeatInterval)
        assertEquals(30, template.notificationDaysBefore)
    }

    @Test
    fun `RETIREMENT template has correct properties`() {
        // When
        val template = EventTemplates.RETIREMENT

        // Then
        assertEquals("Retirement", template.name)
        assertEquals("🏖️", template.emoji)
        assertEquals(EventCategory.RETIREMENT, template.category)
        assertEquals(RepeatInterval.NONE, template.repeatInterval)
        assertEquals(60, template.notificationDaysBefore)
    }

    @Test
    fun `HOLIDAY template has correct properties`() {
        // When
        val template = EventTemplates.HOLIDAY

        // Then
        assertEquals("Holiday", template.name)
        assertEquals("🎄", template.emoji)
        assertEquals(EventCategory.HOLIDAY, template.category)
        assertEquals(RepeatInterval.YEARLY, template.repeatInterval)
        assertEquals(7, template.notificationDaysBefore)
    }

    @Test
    fun `CUSTOM template has correct properties`() {
        // When
        val template = EventTemplates.CUSTOM

        // Then
        assertEquals("Custom Event", template.name)
        assertEquals("📅", template.emoji)
        assertEquals(EventCategory.OTHER, template.category)
        assertEquals(RepeatInterval.NONE, template.repeatInterval)
        assertEquals(1, template.notificationDaysBefore)
    }

    @Test
    fun `all templates have non-empty descriptions`() {
        // When
        val templates = EventTemplates.ALL_TEMPLATES

        // Then
        templates.forEach { template ->
            assertNotNull("${template.name} should have description", template.description)
            assertTrue("${template.name} description should not be empty",
                template.description.isNotBlank())
        }
    }

    @Test
    fun `all templates have valid colors`() {
        // When
        val templates = EventTemplates.ALL_TEMPLATES

        // Then
        templates.forEach { template ->
            assertTrue("${template.name} should have valid color",
                template.color != 0)
        }
    }

    @Test
    fun `all templates have emojis`() {
        // When
        val templates = EventTemplates.ALL_TEMPLATES

        // Then
        templates.forEach { template ->
            assertNotNull("${template.name} should have emoji", template.emoji)
            assertTrue("${template.name} emoji should not be empty",
                template.emoji.isNotBlank())
        }
    }

    @Test
    fun `repeating templates have yearly interval`() {
        // Given
        val repeatingTemplates = listOf(
            EventTemplates.BIRTHDAY,
            EventTemplates.ANNIVERSARY,
            EventTemplates.HOLIDAY
        )

        // Then
        repeatingTemplates.forEach { template ->
            assertEquals("${template.name} should repeat yearly",
                RepeatInterval.YEARLY, template.repeatInterval)
        }
    }

    @Test
    fun `one-time templates have no repeat`() {
        // Given
        val oneTimeTemplates = listOf(
            EventTemplates.WEDDING,
            EventTemplates.VACATION,
            EventTemplates.GRADUATION,
            EventTemplates.RETIREMENT
        )

        // Then
        oneTimeTemplates.forEach { template ->
            assertEquals("${template.name} should not repeat",
                RepeatInterval.NONE, template.repeatInterval)
        }
    }

    @Test
    fun `notification days are reasonable`() {
        // When
        val templates = EventTemplates.ALL_TEMPLATES

        // Then
        templates.forEach { template ->
            assertTrue("${template.name} notification days should be between 1-90",
                template.notificationDaysBefore in 1..90)
        }
    }

    @Test
    fun `templates can be copied`() {
        // Given
        val original = EventTemplates.BIRTHDAY

        // When
        val copy = original.copy(name = "Modified Birthday")

        // Then
        assertEquals("Modified Birthday", copy.name)
        assertEquals(original.category, copy.category)
        assertEquals(original.repeatInterval, copy.repeatInterval)
    }

    @Test
    fun `templates are distinct objects`() {
        // When
        val template1 = EventTemplates.BIRTHDAY
        val template2 = EventTemplates.WEDDING

        // Then
        assertNotEquals(template1.name, template2.name)
        assertNotEquals(template1.emoji, template2.emoji)
    }
}
