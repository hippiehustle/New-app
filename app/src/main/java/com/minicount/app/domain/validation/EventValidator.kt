package com.minicount.app.domain.validation

import com.minicount.app.data.local.entity.Event
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates event data and provides validation errors
 */
@Singleton
class EventValidator @Inject constructor() {

    /**
     * Validation result
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<ValidationError>) : ValidationResult()
    }

    /**
     * Validation error types
     */
    sealed class ValidationError(val message: String) {
        object EmptyTitle : ValidationError("Event title cannot be empty")
        object TitleTooLong : ValidationError("Event title is too long (max 100 characters)")
        object TitleTooShort : ValidationError("Event title is too short (min 2 characters)")
        object DateInPast : ValidationError("Event date cannot be in the past")
        object DateTooFarInFuture : ValidationError("Event date is too far in the future (max 10 years)")
        object NotesTooLong : ValidationError("Notes are too long (max 500 characters)")
        object InvalidPhotoUri : ValidationError("Invalid photo URI")
        data class Custom(val msg: String) : ValidationError(msg)
    }

    companion object {
        private const val MIN_TITLE_LENGTH = 2
        private const val MAX_TITLE_LENGTH = 100
        private const val MAX_NOTES_LENGTH = 500
        private const val MAX_YEARS_IN_FUTURE = 10
    }

    /**
     * Validate an event
     */
    fun validate(event: Event, allowPastDates: Boolean = false): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate title
        when {
            event.title.isBlank() -> errors.add(ValidationError.EmptyTitle)
            event.title.length < MIN_TITLE_LENGTH -> errors.add(ValidationError.TitleTooShort)
            event.title.length > MAX_TITLE_LENGTH -> errors.add(ValidationError.TitleTooLong)
        }

        // Validate date
        val now = LocalDateTime.now()
        val maxFutureDate = now.plusYears(MAX_YEARS_IN_FUTURE.toLong())

        when {
            !allowPastDates && event.targetDate.isBefore(now) -> errors.add(ValidationError.DateInPast)
            event.targetDate.isAfter(maxFutureDate) -> errors.add(ValidationError.DateTooFarInFuture)
        }

        // Validate notes
        if (event.notes.length > MAX_NOTES_LENGTH) {
            errors.add(ValidationError.NotesTooLong)
        }

        // Validate photo URI
        event.photoUri?.let { uri ->
            if (uri.isNotBlank() && !isValidUri(uri)) {
                errors.add(ValidationError.InvalidPhotoUri)
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validate event title only
     */
    fun validateTitle(title: String): ValidationError? {
        return when {
            title.isBlank() -> ValidationError.EmptyTitle
            title.length < MIN_TITLE_LENGTH -> ValidationError.TitleTooShort
            title.length > MAX_TITLE_LENGTH -> ValidationError.TitleTooLong
            else -> null
        }
    }

    /**
     * Validate event date only
     */
    fun validateDate(date: LocalDateTime, allowPastDates: Boolean = false): ValidationError? {
        val now = LocalDateTime.now()
        val maxFutureDate = now.plusYears(MAX_YEARS_IN_FUTURE.toLong())

        return when {
            !allowPastDates && date.isBefore(now) -> ValidationError.DateInPast
            date.isAfter(maxFutureDate) -> ValidationError.DateTooFarInFuture
            else -> null
        }
    }

    /**
     * Validate notes only
     */
    fun validateNotes(notes: String): ValidationError? {
        return if (notes.length > MAX_NOTES_LENGTH) {
            ValidationError.NotesTooLong
        } else {
            null
        }
    }

    /**
     * Check if a URI string is valid
     */
    private fun isValidUri(uri: String): Boolean {
        return try {
            android.net.Uri.parse(uri) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitize event title
     */
    fun sanitizeTitle(title: String): String {
        return title.trim()
            .take(MAX_TITLE_LENGTH)
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Sanitize event notes
     */
    fun sanitizeNotes(notes: String): String {
        return notes.trim()
            .take(MAX_NOTES_LENGTH)
    }

    /**
     * Check if an event is valid for display
     */
    fun isValidForDisplay(event: Event): Boolean {
        return event.title.isNotBlank() &&
                event.title.length >= MIN_TITLE_LENGTH
    }

    /**
     * Get validation constraints as a map
     */
    fun getValidationConstraints(): Map<String, Any> {
        return mapOf(
            "minTitleLength" to MIN_TITLE_LENGTH,
            "maxTitleLength" to MAX_TITLE_LENGTH,
            "maxNotesLength" to MAX_NOTES_LENGTH,
            "maxYearsInFuture" to MAX_YEARS_IN_FUTURE
        )
    }
}
