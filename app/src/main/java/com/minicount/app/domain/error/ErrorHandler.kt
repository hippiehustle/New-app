package com.minicount.app.domain.error

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppError {
    data class DatabaseError(val message: String, val cause: Throwable? = null) : AppError()
    data class NetworkError(val message: String, val cause: Throwable? = null) : AppError()
    data class BillingError(val message: String, val cause: Throwable? = null) : AppError()
    data class FileError(val message: String, val cause: Throwable? = null) : AppError()
    data class ValidationError(val message: String) : AppError()
    data class Unknown(val message: String, val cause: Throwable? = null) : AppError()
}

@Singleton
class ErrorHandler @Inject constructor() {

    private val _errors = MutableStateFlow<AppError?>(null)
    val errors: StateFlow<AppError?> = _errors.asStateFlow()

    fun handleError(error: Throwable, context: String = "") {
        val appError = when (error) {
            is java.io.IOException -> AppError.FileError(
                "File operation failed${if (context.isNotEmpty()) " in $context" else ""}",
                error
            )
            is android.database.SQLException -> AppError.DatabaseError(
                "Database error${if (context.isNotEmpty()) " in $context" else ""}",
                error
            )
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException -> AppError.NetworkError(
                "Network error${if (context.isNotEmpty()) " in $context" else ""}",
                error
            )
            else -> AppError.Unknown(
                error.message ?: "An unknown error occurred${if (context.isNotEmpty()) " in $context" else ""}",
                error
            )
        }

        logError(appError)
        _errors.value = appError
    }

    fun handleAppError(error: AppError) {
        logError(error)
        _errors.value = error
    }

    fun clearError() {
        _errors.value = null
    }

    private fun logError(error: AppError) {
        when (error) {
            is AppError.DatabaseError -> Log.e(TAG, "Database Error: ${error.message}", error.cause)
            is AppError.NetworkError -> Log.e(TAG, "Network Error: ${error.message}", error.cause)
            is AppError.BillingError -> Log.e(TAG, "Billing Error: ${error.message}", error.cause)
            is AppError.FileError -> Log.e(TAG, "File Error: ${error.message}", error.cause)
            is AppError.ValidationError -> Log.w(TAG, "Validation Error: ${error.message}")
            is AppError.Unknown -> Log.e(TAG, "Unknown Error: ${error.message}", error.cause)
        }
    }

    fun getUserMessage(error: AppError): String {
        return when (error) {
            is AppError.DatabaseError -> "Failed to save data. Please try again."
            is AppError.NetworkError -> "Network connection error. Please check your internet."
            is AppError.BillingError -> "Purchase failed. Please try again or contact support."
            is AppError.FileError -> "File operation failed. Please check storage permissions."
            is AppError.ValidationError -> error.message
            is AppError.Unknown -> "An unexpected error occurred. Please try again."
        }
    }

    companion object {
        private const val TAG = "MiniCount_ErrorHandler"
    }
}

// Extension function for safe execution with error handling
suspend fun <T> ErrorHandler.executeSafely(
    context: String = "",
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        handleError(e, context)
        Result.failure(e)
    }
}
