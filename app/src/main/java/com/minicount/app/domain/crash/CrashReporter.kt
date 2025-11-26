package com.minicount.app.domain.crash

import android.content.Context
import android.util.Log
import com.minicount.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash reporting manager ready for Firebase Crashlytics integration
 */
@Singleton
class CrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CrashReporter"
        private const val ENABLED = true
    }

    init {
        if (ENABLED && !BuildConfig.DEBUG) {
            setupCrashReporting()
        }
    }

    /**
     * Initialize crash reporting
     */
    private fun setupCrashReporting() {
        // Set up uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logException(throwable, "Uncaught exception on thread: ${thread.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging crash", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        Log.d(TAG, "Crash reporting initialized")

        // TODO: Initialize Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    /**
     * Log a non-fatal exception
     */
    fun logException(throwable: Throwable, message: String? = null) {
        if (!ENABLED) return

        val logMessage = message ?: "Exception occurred"
        Log.e(TAG, logMessage, throwable)

        // TODO: Report to Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().apply {
        //     if (message != null) {
        //         log(message)
        //     }
        //     recordException(throwable)
        // }
    }

    /**
     * Log a custom message
     */
    fun log(message: String) {
        if (!ENABLED) return

        Log.d(TAG, message)

        // TODO: Add to Crashlytics logs
        // FirebaseCrashlytics.getInstance().log(message)
    }

    /**
     * Set custom key-value pairs for crash context
     */
    fun setCustomKey(key: String, value: String) {
        if (!ENABLED) return

        Log.d(TAG, "Custom Key: $key = $value")

        // TODO: Set in Crashlytics
        // FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        setCustomKey(key, value.toString())
    }

    fun setCustomKey(key: String, value: Int) {
        setCustomKey(key, value.toString())
    }

    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String) {
        if (!ENABLED) return

        Log.d(TAG, "User ID set: $userId")

        // TODO: Set in Crashlytics
        // FirebaseCrashlytics.getInstance().setUserId(userId)
    }

    /**
     * Force a crash (for testing only)
     */
    fun testCrash() {
        if (BuildConfig.DEBUG) {
            throw RuntimeException("Test crash from CrashReporter")
        }
    }

    /**
     * Record breadcrumb for debugging
     */
    fun recordBreadcrumb(category: String, message: String, data: Map<String, String> = emptyMap()) {
        if (!ENABLED) return

        val breadcrumb = "[$category] $message ${data.entries.joinToString { "${it.key}=${it.value}" }}"
        log(breadcrumb)
    }

    /**
     * Track app state
     */
    fun setAppState(state: AppState) {
        setCustomKey("app_state", state.name)
        log("App state changed to: ${state.name}")
    }

    enum class AppState {
        FOREGROUND,
        BACKGROUND,
        ONCREATE,
        ONSTART,
        ONRESUME,
        ONPAUSE,
        ONSTOP,
        ONDESTROY
    }
}

/**
 * Extension function for easier exception logging
 */
fun Throwable.report(crashReporter: CrashReporter, message: String? = null) {
    crashReporter.logException(this, message)
}

/**
 * Safe execution wrapper that catches and reports exceptions
 */
inline fun <T> CrashReporter.safely(
    defaultValue: T,
    action: String = "operation",
    block: () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        logException(e, "Error during $action")
        defaultValue
    }
}

/**
 * Safe execution wrapper for suspend functions
 */
suspend inline fun <T> CrashReporter.safelySuspend(
    defaultValue: T,
    action: String = "operation",
    crossinline block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        logException(e, "Error during $action")
        defaultValue
    }
}
