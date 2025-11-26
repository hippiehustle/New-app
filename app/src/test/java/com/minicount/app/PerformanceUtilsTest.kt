package com.minicount.app

import com.minicount.app.domain.util.Debouncer
import com.minicount.app.domain.util.RateLimiter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceUtilsTest {

    @Test
    fun `RateLimiter allows calls within limit`() = runTest {
        val rateLimiter = RateLimiter(
            windowDuration = 1000.milliseconds,
            maxCalls = 3
        )

        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
    }

    @Test
    fun `RateLimiter blocks calls exceeding limit`() = runTest {
        val rateLimiter = RateLimiter(
            windowDuration = 1000.milliseconds,
            maxCalls = 2
        )

        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire()) // Should be blocked
    }

    @Test
    fun `RateLimiter resets after time window`() = runTest {
        val rateLimiter = RateLimiter(
            windowDuration = 100.milliseconds,
            maxCalls = 2
        )

        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire())

        delay(150) // Wait for window to expire

        assertTrue(rateLimiter.tryAcquire()) // Should work again
    }

    @Test
    fun `RateLimiter reset clears all calls`() = runTest {
        val rateLimiter = RateLimiter(
            windowDuration = 1000.milliseconds,
            maxCalls = 2
        )

        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire())

        rateLimiter.reset()

        assertTrue(rateLimiter.tryAcquire()) // Should work after reset
    }

    @Test
    fun `Debouncer executes action after delay`() = runTest {
        var executed = false
        val debouncer = Debouncer(delayMs = 100, scope = this)

        debouncer.debounce {
            executed = true
        }

        assertFalse(executed) // Should not execute immediately

        delay(150) // Wait for debounce delay

        assertTrue(executed) // Should be executed now
    }

    @Test
    fun `Debouncer cancels previous action when called again`() = runTest {
        var counter = 0
        val debouncer = Debouncer(delayMs = 100, scope = this)

        debouncer.debounce { counter++ }
        delay(50) // Wait less than debounce delay
        debouncer.debounce { counter++ } // Should cancel previous

        delay(150) // Wait for second debounce to complete

        assertEquals(1, counter) // Only second action should execute
    }

    @Test
    fun `Debouncer cancel prevents execution`() = runTest {
        var executed = false
        val debouncer = Debouncer(delayMs = 100, scope = this)

        debouncer.debounce {
            executed = true
        }

        debouncer.cancel()

        delay(150)

        assertFalse(executed) // Should not execute after cancel
    }
}
