package com.minicount.app.domain.error

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Global error handling system for the application.
 *
 * Provides:
 * - Centralized error logging
 * - Error categorization
 * - User-friendly error messages
 * - Coroutine exception handling
 */
object GlobalErrorHandler {

    private const val TAG = "GlobalErrorHandler"

    /**
     * Listener for error events.
     * Implement this to show error messages to users (e.g., SnackBar, Toast).
     */
    interface ErrorListener {
        fun onError(error: AppError)
    }

    private var errorListener: ErrorListener? = null

    /**
     * Sets the global error listener.
     *
     * @param listener Listener to handle error events
     */
    fun setErrorListener(listener: ErrorListener?) {
        errorListener = listener
    }

    /**
     * Handles an error and notifies the listener.
     *
     * @param throwable The exception that occurred
     * @param context Optional context about where error occurred
     */
    fun handleError(throwable: Throwable, context: String = "Unknown") {
        val appError = categorizeError(throwable, context)

        // Log the error
        when (appError.severity) {
            ErrorSeverity.CRITICAL -> Log.e(TAG, "[$context] ${appError.message}", throwable)
            ErrorSeverity.HIGH -> Log.w(TAG, "[$context] ${appError.message}", throwable)
            ErrorSeverity.MEDIUM -> Log.w(TAG, "[$context] ${appError.message}")
            ErrorSeverity.LOW -> Log.d(TAG, "[$context] ${appError.message}")
        }

        // Notify listener
        errorListener?.onError(appError)
    }

    /**
     * Categorizes an exception into an AppError with appropriate message and severity.
     *
     * @param throwable The exception to categorize
     * @param context Where the error occurred
     * @return AppError with user-friendly message
     */
    private fun categorizeError(throwable: Throwable, context: String): AppError {
        return when (throwable) {
            is java.io.IOException -> AppError(
                type = ErrorType.NETWORK,
                message = "Network error. Please check your connection.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.HIGH
            )

            is java.net.UnknownHostException -> AppError(
                type = ErrorType.NETWORK,
                message = "Unable to connect. Please check your internet connection.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.HIGH
            )

            is java.net.SocketTimeoutException -> AppError(
                type = ErrorType.NETWORK,
                message = "Connection timed out. Please try again.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.MEDIUM
            )

            is android.database.sqlite.SQLiteException -> AppError(
                type = ErrorType.DATABASE,
                message = "Database error. Please restart the app.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.CRITICAL
            )

            is IllegalArgumentException, is IllegalStateException -> AppError(
                type = ErrorType.VALIDATION,
                message = throwable.message ?: "Invalid input. Please check and try again.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.MEDIUM
            )

            is SecurityException -> AppError(
                type = ErrorType.PERMISSION,
                message = "Permission denied. Please grant required permissions.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.HIGH
            )

            is OutOfMemoryError -> AppError(
                type = ErrorType.MEMORY,
                message = "Low memory. Please close some apps and try again.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.CRITICAL
            )

            else -> AppError(
                type = ErrorType.UNKNOWN,
                message = "An unexpected error occurred. Please try again.",
                originalException = throwable,
                context = context,
                severity = ErrorSeverity.HIGH
            )
        }
    }

    /**
     * Creates a CoroutineExceptionHandler for handling uncaught exceptions in coroutines.
     *
     * Usage:
     * ```kotlin
     * viewModelScope.launch(GlobalErrorHandler.coroutineExceptionHandler("MyViewModel")) {
     *     // Coroutine code
     * }
     * ```
     *
     * @param context Context identifier for error logging
     * @return CoroutineExceptionHandler
     */
    fun coroutineExceptionHandler(context: String): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            handleError(throwable, context)
        }
    }
}

/**
 * Represents an application error with categorization and context.
 *
 * @property type Category of the error
 * @property message User-friendly error message
 * @property originalException The original exception that caused the error
 * @property context Where the error occurred
 * @property severity How severe the error is
 */
data class AppError(
    val type: ErrorType,
    val message: String,
    val originalException: Throwable,
    val context: String,
    val severity: ErrorSeverity
) {
    /**
     * Gets the full error details for logging.
     */
    fun getDetails(): String {
        return """
            Error Type: $type
            Message: $message
            Context: $context
            Severity: $severity
            Exception: ${originalException.javaClass.simpleName}
            Stack Trace: ${originalException.stackTraceToString()}
        """.trimIndent()
    }
}

/**
 * Categories of errors that can occur in the app.
 */
enum class ErrorType {
    /** Network-related errors (no connection, timeout, etc.) */
    NETWORK,

    /** Database errors (SQLite exceptions, corruption, etc.) */
    DATABASE,

    /** Validation errors (invalid input, illegal arguments) */
    VALIDATION,

    /** Permission errors (security exceptions) */
    PERMISSION,

    /** Memory errors (OOM, etc.) */
    MEMORY,

    /** Billing/purchase errors */
    BILLING,

    /** Unknown or uncategorized errors */
    UNKNOWN
}

/**
 * Severity levels for errors.
 */
enum class ErrorSeverity {
    /** Low severity - informational, app can continue normally */
    LOW,

    /** Medium severity - non-critical issue, app can continue with degraded functionality */
    MEDIUM,

    /** High severity - significant issue, feature may not work */
    HIGH,

    /** Critical severity - app may crash or become unusable */
    CRITICAL
}
