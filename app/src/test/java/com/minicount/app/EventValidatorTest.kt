package com.minicount.app

import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.domain.validation.EventValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class EventValidatorTest {

    private lateinit var validator: EventValidator

    @Before
    fun setup() {
        validator = EventValidator()
    }

    @Test
    fun `valid event passes validation`() {
        val event = Event(
            title = "Valid Event",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Valid)
    }

    @Test
    fun `empty title fails validation`() {
        val event = Event(
            title = "",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.EmptyTitle })
    }

    @Test
    fun `short title fails validation`() {
        val event = Event(
            title = "A",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.TitleTooShort })
    }

    @Test
    fun `long title fails validation`() {
        val event = Event(
            title = "A".repeat(150),
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.TitleTooLong })
    }

    @Test
    fun `past date fails validation when not allowed`() {
        val event = Event(
            title = "Past Event",
            targetDate = LocalDateTime.now().minusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event, allowPastDates = false)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.DateInPast })
    }

    @Test
    fun `past date passes validation when allowed`() {
        val event = Event(
            title = "Past Event",
            targetDate = LocalDateTime.now().minusDays(7),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event, allowPastDates = true)
        assertTrue(result is EventValidator.ValidationResult.Valid)
    }

    @Test
    fun `far future date fails validation`() {
        val event = Event(
            title = "Far Future Event",
            targetDate = LocalDateTime.now().plusYears(15),
            category = EventCategory.BIRTHDAY
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.DateTooFarInFuture })
    }

    @Test
    fun `long notes fail validation`() {
        val event = Event(
            title = "Valid Event",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY,
            notes = "A".repeat(600)
        )

        val result = validator.validate(event)
        assertTrue(result is EventValidator.ValidationResult.Invalid)

        val errors = (result as EventValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it is EventValidator.ValidationError.NotesTooLong })
    }

    @Test
    fun `validateTitle returns null for valid title`() {
        val error = validator.validateTitle("Valid Title")
        assertNull(error)
    }

    @Test
    fun `validateTitle returns error for empty title`() {
        val error = validator.validateTitle("")
        assertNotNull(error)
        assertTrue(error is EventValidator.ValidationError.EmptyTitle)
    }

    @Test
    fun `sanitizeTitle trims and limits length`() {
        val longTitle = "  " + "A".repeat(150) + "  "
        val sanitized = validator.sanitizeTitle(longTitle)

        assertEquals(100, sanitized.length)
        assertFalse(sanitized.startsWith(" "))
        assertFalse(sanitized.endsWith(" "))
    }

    @Test
    fun `sanitizeNotes limits length`() {
        val longNotes = "A".repeat(600)
        val sanitized = validator.sanitizeNotes(longNotes)

        assertEquals(500, sanitized.length)
    }

    @Test
    fun `isValidForDisplay returns true for valid event`() {
        val event = Event(
            title = "Valid Event",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        assertTrue(validator.isValidForDisplay(event))
    }

    @Test
    fun `isValidForDisplay returns false for invalid event`() {
        val event = Event(
            title = "A",
            targetDate = LocalDateTime.now().plusDays(7),
            category = EventCategory.BIRTHDAY
        )

        assertFalse(validator.isValidForDisplay(event))
    }
}
