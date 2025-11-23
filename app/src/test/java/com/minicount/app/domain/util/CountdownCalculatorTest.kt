package com.minicount.app.domain.util

import com.minicount.app.data.local.entity.RepeatInterval
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class CountdownCalculatorTest {

    @Test
    fun `calculate countdown for future date returns correct values`() {
        val now = LocalDateTime.of(2024, 1, 1, 0, 0)
        val target = LocalDateTime.of(2024, 1, 31, 12, 30)

        // Mock current time (in actual implementation, would use dependency injection)
        val result = CountdownCalculator.calculate(target)

        assertTrue("Should not be past", !result.isPast)
        assertEquals("Should have 30 total days", 30, result.totalDays)
    }

    @Test
    fun `calculate countdown for past date returns correct values`() {
        val now = LocalDateTime.of(2024, 1, 31, 0, 0)
        val target = LocalDateTime.of(2024, 1, 1, 0, 0)

        val result = CountdownCalculator.calculate(target)

        assertTrue("Should be past", result.isPast)
        assertEquals("Should have 30 total days", 30, result.totalDays)
    }

    @Test
    fun `formatCountdown returns correct string format`() {
        val countdown = CountdownData(
            years = 1,
            months = 2,
            days = 15,
            hours = 6,
            minutes = 30,
            seconds = 45,
            isPast = false,
            totalDays = 440
        )

        val result = CountdownCalculator.formatCountdown(countdown)

        assertTrue("Should include years", result.contains("1y"))
        assertTrue("Should include months", result.contains("2mo"))
        assertTrue("Should include days", result.contains("15d"))
        assertTrue("Should include hours", result.contains("6h"))
    }

    @Test
    fun `formatCountdown without seconds omits seconds`() {
        val countdown = CountdownData(
            years = 0,
            months = 0,
            days = 5,
            hours = 3,
            minutes = 20,
            seconds = 45,
            isPast = false,
            totalDays = 5
        )

        val result = CountdownCalculator.formatCountdown(countdown, showSeconds = false)

        assertFalse("Should not include seconds", result.contains("45s"))
    }

    @Test
    fun `formatShort returns appropriate format based on largest unit`() {
        val yearsCountdown = CountdownData(
            years = 2,
            months = 3,
            days = 0,
            hours = 0,
            minutes = 0,
            seconds = 0,
            isPast = false,
            totalDays = 823
        )

        val result = CountdownCalculator.formatShort(yearsCountdown)
        assertTrue("Should show years and months", result.contains("2y") && result.contains("3mo"))
    }

    @Test
    fun `getNextOccurrence for yearly returns next year`() {
        val baseDate = LocalDateTime.of(2023, 6, 15, 10, 0)

        val next = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.YEARLY)

        assertTrue("Next occurrence should be in future", next.isAfter(LocalDateTime.now()))
        assertEquals("Should be same month", 6, next.monthValue)
        assertEquals("Should be same day", 15, next.dayOfMonth)
    }

    @Test
    fun `getNextOccurrence for monthly returns next month`() {
        val baseDate = LocalDateTime.of(2023, 6, 15, 10, 0)

        val next = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.MONTHLY)

        assertTrue("Next occurrence should be in future", next.isAfter(LocalDateTime.now()))
        assertEquals("Should be same day of month", 15, next.dayOfMonth)
    }

    @Test
    fun `getNextOccurrence for NONE returns original date`() {
        val baseDate = LocalDateTime.of(2023, 6, 15, 10, 0)

        val next = CountdownCalculator.getNextOccurrence(baseDate, RepeatInterval.NONE)

        assertEquals("Should return original date", baseDate, next)
    }
}
