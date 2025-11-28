package com.minicount.app.domain

import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.domain.util.CountdownCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class CountdownCalculatorTest {

    @Test
    fun `calculate should handle future dates correctly`() {
        // Given
        val futureDate = LocalDateTime.now().plusDays(7).plusHours(5).plusMinutes(30)

        // When
        val result = CountdownCalculator.calculate(futureDate)

        // Then
        assertFalse(result.isPast)
        assertEquals(7, result.days)
        assertEquals(5, result.hours)
        assertEquals(30, result.minutes)
        assertEquals(7, result.totalDays)
    }

    @Test
    fun `calculate should handle past dates correctly`() {
        // Given
        val pastDate = LocalDateTime.now().minusDays(3).minusHours(2)

        // When
        val result = CountdownCalculator.calculate(pastDate)

        // Then
        assertTrue(result.isPast)
        assertEquals(3, result.days)
        assertEquals(2, result.hours)
        assertEquals(3, result.totalDays)
    }

    @Test
    fun `calculate should handle dates more than a year away`() {
        // Given
        val farFutureDate = LocalDateTime.now().plusYears(2).plusMonths(3).plusDays(15)

        // When
        val result = CountdownCalculator.calculate(farFutureDate)

        // Then
        assertFalse(result.isPast)
        assertEquals(2, result.years)
        assertEquals(3, result.months)
        assertEquals(15, result.days)
    }

    @Test
    fun `formatCountdown should format years months and days`() {
        // Given
        val futureDate = LocalDateTime.now().plusYears(1).plusMonths(2).plusDays(5)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatCountdown(data)

        // Then
        assertTrue(formatted.contains("1y"))
        assertTrue(formatted.contains("2mo"))
        assertTrue(formatted.contains("5d"))
    }

    @Test
    fun `formatCountdown should include seconds when requested`() {
        // Given
        val futureDate = LocalDateTime.now().plusMinutes(5).plusSeconds(30)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatCountdown(data, showSeconds = true)

        // Then
        assertTrue(formatted.contains("5m"))
        assertTrue(formatted.contains("30s"))
    }

    @Test
    fun `formatCountdown should exclude seconds by default`() {
        // Given
        val futureDate = LocalDateTime.now().plusMinutes(5).plusSeconds(30)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatCountdown(data, showSeconds = false)

        // Then
        assertTrue(formatted.contains("5m"))
        assertFalse(formatted.contains("s"))
    }

    @Test
    fun `formatShort should show years and months for long durations`() {
        // Given
        val futureDate = LocalDateTime.now().plusYears(3).plusMonths(6)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatShort(data)

        // Then
        assertTrue(formatted.contains("3y"))
        assertTrue(formatted.contains("6mo"))
        assertFalse(formatted.contains("d")) // Should not include days
    }

    @Test
    fun `formatShort should show months and days for medium durations`() {
        // Given
        val futureDate = LocalDateTime.now().plusMonths(5).plusDays(10)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatShort(data)

        // Then
        assertTrue(formatted.contains("mo"))
        assertTrue(formatted.contains("d"))
        assertFalse(formatted.contains("y"))
        assertFalse(formatted.contains("h"))
    }

    @Test
    fun `formatShort should show days and hours for short durations`() {
        // Given
        val futureDate = LocalDateTime.now().plusDays(5).plusHours(8)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatShort(data)

        // Then
        assertTrue(formatted.contains("d"))
        assertTrue(formatted.contains("h"))
        assertFalse(formatted.contains("mo"))
    }

    @Test
    fun `formatShort should show hours and minutes for very short durations`() {
        // Given
        val futureDate = LocalDateTime.now().plusHours(3).plusMinutes(25)
        val data = CountdownCalculator.calculate(futureDate)

        // When
        val formatted = CountdownCalculator.formatShort(data)

        // Then
        assertTrue(formatted.contains("h"))
        assertTrue(formatted.contains("m"))
        assertFalse(formatted.contains("d"))
    }

    @Test
    fun `getNextOccurrence should return same date for NONE interval`() {
        // Given
        val baseDate = LocalDateTime.now().minusDays(10)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.NONE)

        // Then
        assertEquals(baseDate, nextOccurrence)
    }

    @Test
    fun `getNextOccurrence should calculate next daily occurrence`() {
        // Given
        val baseDate = LocalDateTime.now().minusDays(3)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.DAILY)

        // Then
        assertTrue(nextOccurrence.isAfter(LocalDateTime.now()))
        // Should be less than 1 day in the future
        val diff = java.time.Duration.between(LocalDateTime.now(), nextOccurrence).toHours()
        assertTrue(diff < 24)
    }

    @Test
    fun `getNextOccurrence should calculate next weekly occurrence`() {
        // Given
        val baseDate = LocalDateTime.now().minusWeeks(2)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.WEEKLY)

        // Then
        assertTrue(nextOccurrence.isAfter(LocalDateTime.now()))
        // Should be less than 7 days in the future
        val diff = java.time.Duration.between(LocalDateTime.now(), nextOccurrence).toDays()
        assertTrue(diff < 7)
    }

    @Test
    fun `getNextOccurrence should calculate next monthly occurrence`() {
        // Given
        val baseDate = LocalDateTime.now().minusMonths(3)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.MONTHLY)

        // Then
        assertTrue(nextOccurrence.isAfter(LocalDateTime.now()))
        // Should be less than 31 days in the future
        val diff = java.time.Duration.between(LocalDateTime.now(), nextOccurrence).toDays()
        assertTrue(diff < 31)
    }

    @Test
    fun `getNextOccurrence should calculate next yearly occurrence`() {
        // Given
        val baseDate = LocalDateTime.now().minusYears(2)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.YEARLY)

        // Then
        assertTrue(nextOccurrence.isAfter(LocalDateTime.now()))
        // Should be less than 365 days in the future
        val diff = java.time.Duration.between(LocalDateTime.now(), nextOccurrence).toDays()
        assertTrue(diff < 365)
    }

    @Test
    fun `getNextOccurrence should return future date if already in future`() {
        // Given
        val futureDate = LocalDateTime.now().plusDays(10)

        // When
        val nextOccurrence = CountdownCalculator.getNextOccurrence(futureDate, RepeatInterval.DAILY)

        // Then
        assertEquals(futureDate, nextOccurrence)
    }

    @Test
    fun `calculate should handle exact day boundary`() {
        // Given
        val exactDayFuture = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)

        // When
        val result = CountdownCalculator.calculate(exactDayFuture)

        // Then
        assertFalse(result.isPast)
        assertTrue(result.totalDays >= 0)
    }

    @Test
    fun `calculate should handle same time`() {
        // Given
        val now = LocalDateTime.now()

        // When
        val result = CountdownCalculator.calculate(now)

        // Then
        assertEquals(0, result.totalDays)
        assertEquals(0, result.days)
        assertEquals(0, result.hours)
        assertEquals(0, result.minutes)
    }
}
