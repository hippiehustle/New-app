package com.minicount.app.presentation.common

/**
 * Sealed class representing UI state for ViewModels
 * Provides consistent state management across the app
 *
 * @param T The type of data to be held in Success state
 */
sealed class UiState<out T> {
    /**
     * Initial/Loading state - data is being fetched
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state - data loaded successfully
     * @param data The loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state - an error occurred
     * @param message User-friendly error message
     * @param exception Optional exception for logging
     * @param canRetry Whether the operation can be retried
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : UiState<Nothing>()

    /**
     * Empty state - no data available
     * @param message Optional message to display
     */
    data class Empty(val message: String = "No data available") : UiState<Nothing>()

    /**
     * Helper methods
     */
    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isError() = this is Error
    fun isEmpty() = this is Empty

    /**
     * Get data or null
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get error or null
     */
    fun getErrorOrNull(): Error? = when (this) {
        is Error -> this
        else -> null
    }
}

/**
 * Action state for operations (save, delete, etc.)
 */
sealed class ActionState {
    object Idle : ActionState()
    object InProgress : ActionState()
    data class Success(val message: String = "Operation successful") : ActionState()
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : ActionState()

    fun isInProgress() = this is InProgress
    fun isSuccess() = this is Success
    fun isError() = this is Error
}

/**
 * Extension functions for UiState
 */

/**
 * Transform Success data
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> this
        is UiState.Empty -> this
    }
}

/**
 * Handle each state with lambdas
 */
inline fun <T> UiState<T>.onEach(
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String, Throwable?) -> Unit = { _, _ -> },
    onEmpty: () -> Unit = {}
) {
    when (this) {
        is UiState.Loading -> onLoading()
        is UiState.Success -> onSuccess(data)
        is UiState.Error -> onError(message, exception)
        is UiState.Empty -> onEmpty()
    }
}

/**
 * Convert nullable result to UiState
 */
fun <T> T?.toUiState(emptyMessage: String = "No data"): UiState<T> {
    return if (this != null) {
        UiState.Success(this)
    } else {
        UiState.Empty(emptyMessage)
    }
}

/**
 * Convert Result to UiState
 */
fun <T> Result<T>.toUiState(
    errorMessageMapper: (Throwable) -> String = { it.message ?: "Unknown error" }
): UiState<T> {
    return fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { UiState.Error(errorMessageMapper(it), it) }
    )
}

/**
 * Convert List to UiState with empty check
 */
fun <T> List<T>.toUiState(emptyMessage: String = "No items"): UiState<List<T>> {
    return if (isNotEmpty()) {
        UiState.Success(this)
    } else {
        UiState.Empty(emptyMessage)
    }
}
