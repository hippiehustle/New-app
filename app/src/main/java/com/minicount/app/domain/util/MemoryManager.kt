package com.minicount.app.domain.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.lang.ref.WeakReference

/**
 * Utility for monitoring and managing app memory usage.
 *
 * Provides:
 * - Memory usage monitoring
 * - Low memory warnings
 * - Cache cleanup suggestions
 * - Memory leak detection helpers
 */
object MemoryManager {

    private const val TAG = "MemoryManager"

    /**
     * Threshold for low memory warning (80% of max heap).
     */
    private const val LOW_MEMORY_THRESHOLD = 0.8

    /**
     * Checks if app is using high amount of memory.
     *
     * @return true if memory usage exceeds 80% of max heap
     */
    fun isMemoryLow(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryPercentage = usedMemory.toDouble() / maxMemory.toDouble()

        return memoryPercentage > LOW_MEMORY_THRESHOLD
    }

    /**
     * Gets current memory usage statistics.
     *
     * @return MemoryStats object with current usage information
     */
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        return MemoryStats(
            maxMemoryMB = maxMemory / (1024 * 1024),
            usedMemoryMB = usedMemory / (1024 * 1024),
            freeMemoryMB = freeMemory / (1024 * 1024),
            percentageUsed = (usedMemory.toDouble() / maxMemory.toDouble() * 100).toInt()
        )
    }

    /**
     * Logs current memory usage for debugging.
     */
    fun logMemoryUsage() {
        val stats = getMemoryStats()
        Log.d(
            TAG,
            "Memory: ${stats.usedMemoryMB}MB / ${stats.maxMemoryMB}MB " +
                    "(${stats.percentageUsed}% used, ${stats.freeMemoryMB}MB free)"
        )

        if (isMemoryLow()) {
            Log.w(TAG, "Memory usage is high! Consider clearing caches or optimizing.")
        }
    }

    /**
     * Suggests running garbage collection if memory is low.
     * Note: This only hints to the system, doesn't force GC.
     */
    fun suggestGarbageCollection() {
        if (isMemoryLow()) {
            Log.d(TAG, "Suggesting garbage collection due to high memory usage")
            System.gc()
        }
    }

    /**
     * Clears image cache directory to free memory.
     *
     * @param context Android context
     * @return Number of bytes freed
     */
    fun clearImageCache(context: Context): Long {
        var freedBytes = 0L
        try {
            val cacheDir = context.cacheDir
            val imageFiles = cacheDir.listFiles { file ->
                file.extension in listOf("jpg", "jpeg", "png", "webp")
            } ?: emptyArray()

            for (file in imageFiles) {
                freedBytes += file.length()
                file.delete()
            }

            Log.d(TAG, "Cleared ${imageFiles.size} cached images, freed ${freedBytes / 1024}KB")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing image cache", e)
        }

        return freedBytes
    }

    /**
     * Gets available device memory (system-wide).
     *
     * @param context Android context
     * @return MemoryInfo with system memory stats
     */
    fun getDeviceMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }

    /**
     * Checks if device is in low memory condition.
     *
     * @param context Android context
     * @return true if device has low memory
     */
    fun isDeviceMemoryLow(context: Context): Boolean {
        return getDeviceMemoryInfo(context).lowMemory
    }

    /**
     * Creates a weak reference wrapper to help prevent memory leaks.
     *
     * Weak references allow garbage collection even if reference exists.
     * Useful for callbacks, listeners, etc.
     *
     * @param T Type of object to weakly reference
     * @param obj Object to create weak reference for
     * @return WeakReference wrapper
     */
    fun <T> weakRef(obj: T): WeakReference<T> {
        return WeakReference(obj)
    }
}

/**
 * Data class representing app memory statistics.
 *
 * @property maxMemoryMB Maximum heap size in megabytes
 * @property usedMemoryMB Currently used memory in megabytes
 * @property freeMemoryMB Currently free memory in megabytes
 * @property percentageUsed Percentage of max heap currently used
 */
data class MemoryStats(
    val maxMemoryMB: Long,
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
    val percentageUsed: Int
) {
    /**
     * Check if memory usage is in warning zone (>60%).
     */
    fun isWarning(): Boolean = percentageUsed > 60

    /**
     * Check if memory usage is critical (>80%).
     */
    fun isCritical(): Boolean = percentageUsed > 80

    override fun toString(): String {
        return "${usedMemoryMB}MB / ${maxMemoryMB}MB ($percentageUsed%)"
    }
}
