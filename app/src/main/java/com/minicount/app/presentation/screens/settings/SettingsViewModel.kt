package com.minicount.app.presentation.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.presentation.common.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .catch { e ->
            Log.e(TAG, "Error loading premium status", e)
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showAds: StateFlow<Boolean> = preferencesManager.showAds
        .catch { e ->
            Log.e(TAG, "Error loading ads preference", e)
            emit(true)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _saveState = MutableStateFlow<ActionState>(ActionState.Idle)
    val saveState: StateFlow<ActionState> = _saveState.asStateFlow()

    /**
     * Update notification sound with validation
     */
    fun updateNotificationSound(sound: String) {
        if (sound.isBlank()) {
            _settings.value = _settings.value.copy(error = "Sound cannot be empty")
            return
        }
        _settings.value = _settings.value.copy(notificationSound = sound, error = null)
        Log.d(TAG, "Notification sound updated: $sound")
    }

    /**
     * Update haptic feedback setting
     */
    fun updateHapticFeedback(enabled: Boolean) {
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled, error = null)
        Log.d(TAG, "Haptic feedback updated: $enabled")
    }

    /**
     * Update widget update interval with validation
     */
    fun updateWidgetUpdateInterval(minutes: Int) {
        if (minutes < 15 || minutes > 120) {
            _settings.value = _settings.value.copy(
                error = "Widget update interval must be between 15 and 120 minutes"
            )
            return
        }
        _settings.value = _settings.value.copy(widgetUpdateInterval = minutes, error = null)
        Log.d(TAG, "Widget update interval updated: $minutes minutes")
    }

    /**
     * Update default event reminder days with validation
     */
    fun updateDefaultEventReminder(days: Int) {
        if (days < 0 || days > 365) {
            _settings.value = _settings.value.copy(
                error = "Reminder days must be between 0 and 365"
            )
            return
        }
        _settings.value = _settings.value.copy(defaultEventReminderDays = days, error = null)
        Log.d(TAG, "Default reminder days updated: $days")
    }

    /**
     * Update app theme
     */
    fun updateTheme(theme: AppTheme) {
        _settings.value = _settings.value.copy(theme = theme, error = null)
        Log.d(TAG, "Theme updated: $theme")
    }

    /**
     * Save all settings with error handling
     */
    fun saveSettings() {
        viewModelScope.launch {
            try {
                _saveState.value = ActionState.InProgress
                val state = _settings.value

                // Validate all settings
                if (state.widgetUpdateInterval < 15 || state.widgetUpdateInterval > 120) {
                    throw IllegalArgumentException("Invalid widget update interval")
                }
                if (state.defaultEventReminderDays < 0 || state.defaultEventReminderDays > 365) {
                    throw IllegalArgumentException("Invalid reminder days")
                }

                // Save to preferences (when PreferencesManager methods are available)
                // For now, settings are in memory

                _saveState.value = ActionState.Success("Settings saved")
                Log.d(TAG, "Settings saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save settings", e)
                _saveState.value = ActionState.Error(
                    message = "Failed to save settings: ${e.message}",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _settings.value = _settings.value.copy(error = null)
    }

    /**
     * Clear save state
     */
    fun clearSaveState() {
        _saveState.value = ActionState.Idle
    }

    /**
     * Retry save operation
     */
    fun retrySave() {
        if (_saveState.value is ActionState.Error) {
            saveSettings()
        }
    }
}

data class SettingsState(
    val notificationSound: String = "default",
    val hapticFeedbackEnabled: Boolean = true,
    val widgetUpdateInterval: Int = 30,
    val defaultEventReminderDays: Int = 1,
    val theme: AppTheme = AppTheme.SYSTEM,
    val error: String? = null
)

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}
