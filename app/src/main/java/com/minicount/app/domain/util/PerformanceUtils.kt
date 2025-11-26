package com.minicount.app.domain.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Performance utilities for optimizing app responsiveness and reducing unnecessary operations
 */
object PerformanceUtils {

    /**
     * Debounces a flow of values, emitting only after a specified delay with no new emissions
     * Useful for search queries, text input, etc.
     */
    fun <T> Flow<T>.debounceFlow(timeout: Duration = 300.milliseconds): Flow<T> {
        return this.debounce(timeout.inWholeMilliseconds)
    }

    /**
     * Throttles a flow to emit at most one value per specified time window
     * Useful for limiting rapid updates like scroll events
     */
    fun <T> Flow<T>.throttleFirst(windowDuration: Duration = 300.milliseconds): Flow<T> = flow {
        var lastEmitTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmitTime >= windowDuration.inWholeMilliseconds) {
                lastEmitTime = currentTime
                emit(value)
            }
        }
    }

    /**
     * Caches the latest value of a flow for a specified duration
     */
    fun <T> Flow<T>.cacheFor(duration: Duration = 5000.milliseconds): Flow<T> {
        return this.shareIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(duration.inWholeMilliseconds),
            replay = 1
        )
    }
}

/**
 * Rate limiter for function calls
 */
class RateLimiter(
    private val windowDuration: Duration = 1000.milliseconds,
    private val maxCalls: Int = 5
) {
    private val callTimes = mutableListOf<Long>()

    /**
     * Returns true if the call is allowed, false if rate limited
     */
    fun tryAcquire(): Boolean {
        val now = System.currentTimeMillis()

        // Remove old entries outside the time window
        callTimes.removeAll { it < now - windowDuration.inWholeMilliseconds }

        return if (callTimes.size < maxCalls) {
            callTimes.add(now)
            true
        } else {
            false
        }
    }

    /**
     * Suspends until the call can be made
     */
    suspend fun acquire() {
        while (!tryAcquire()) {
            delay(100)
        }
    }

    /**
     * Resets the rate limiter
     */
    fun reset() {
        callTimes.clear()
    }
}

/**
 * Debouncer for non-flow function calls
 */
class Debouncer(
    private val delayMs: Long = 300L,
    private val scope: CoroutineScope
) {
    private var debounceJob: Job? = null

    /**
     * Executes the action after the debounce delay
     */
    fun debounce(action: () -> Unit) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            action()
        }
    }

    /**
     * Cancels any pending debounced action
     */
    fun cancel() {
        debounceJob?.cancel()
    }
}

/**
 * Lazy initializer with thread-safe memoization
 */
class LazyMemoized<T>(private val initializer: () -> T) {
    private var cached: T? = null
    private var initialized = false

    @Synchronized
    fun get(): T {
        if (!initialized) {
            cached = initializer()
            initialized = true
        }
        return cached!!
    }

    @Synchronized
    fun invalidate() {
        cached = null
        initialized = false
    }
}

/**
 * Memory-efficient list batching
 */
fun <T> List<T>.chunkedProcess(
    chunkSize: Int = 100,
    process: (List<T>) -> Unit
) {
    chunked(chunkSize).forEach { chunk ->
        process(chunk)
    }
}

/**
 * Composable function debouncing
 */
@androidx.compose.runtime.Composable
fun <T> rememberDebounced(
    value: T,
    delayMs: Long = 300L
): androidx.compose.runtime.State<T> {
    val state = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(value) }

    androidx.compose.runtime.LaunchedEffect(value) {
        delay(delayMs)
        state.value = value
    }

    return state
}

/**
 * Image loading optimization - calculates optimal sample size
 */
object ImageOptimizer {
    fun calculateInSampleSize(
        imageWidth: Int,
        imageHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (imageHeight > reqHeight || imageWidth > reqWidth) {
            val halfHeight = imageHeight / 2
            val halfWidth = imageWidth / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
