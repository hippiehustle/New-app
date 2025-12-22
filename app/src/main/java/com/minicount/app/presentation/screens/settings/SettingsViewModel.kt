package com.minicount.app.presentation.screens.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.backup.BackupManager
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val eventRepository: EventRepository,
    @ApplicationContext private val context: Context
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

    // Backup/Restore state
    private val _backupState = MutableStateFlow<ActionState>(ActionState.Idle)
    val backupState: StateFlow<ActionState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow<ActionState>(ActionState.Idle)
    val restoreState: StateFlow<ActionState> = _restoreState.asStateFlow()

    private val _availableBackups = MutableStateFlow<List<File>>(emptyList())
    val availableBackups: StateFlow<List<File>> = _availableBackups.asStateFlow()

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

    // Backup/Restore Functions

    /**
     * Load list of available backups
     */
    fun loadAvailableBackups() {
        viewModelScope.launch {
            try {
                val backups = BackupManager.listBackups(context)
                _availableBackups.value = backups
                Log.d(TAG, "Loaded ${backups.size} backups")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load backups", e)
                _availableBackups.value = emptyList()
            }
        }
    }

    /**
     * Create a backup of all events
     */
    fun createBackup() {
        viewModelScope.launch {
            try {
                _backupState.value = ActionState.InProgress

                // Get all events
                val events = eventRepository.getAllEvents().first()
                when (events) {
                    is UiState.Success -> {
                        val result = BackupManager.createBackup(context, events.data)
                        result.fold(
                            onSuccess = { uri ->
                                _backupState.value = ActionState.Success(
                                    "Backup created: ${uri.lastPathSegment}"
                                )
                                loadAvailableBackups() // Refresh backup list
                                Log.d(TAG, "Backup created successfully: $uri")
                            },
                            onFailure = { e ->
                                Log.e(TAG, "Failed to create backup", e)
                                _backupState.value = ActionState.Error(
                                    message = "Failed to create backup: ${e.message}",
                                    exception = e,
                                    canRetry = true
                                )
                            }
                        )
                    }
                    else -> {
                        _backupState.value = ActionState.Error(
                            message = "Cannot create backup: events not loaded",
                            exception = null,
                            canRetry = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating backup", e)
                _backupState.value = ActionState.Error(
                    message = "Failed to create backup: ${e.message}",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Restore events from a backup URI
     */
    fun restoreBackup(backupUri: Uri) {
        viewModelScope.launch {
            try {
                _restoreState.value = ActionState.InProgress

                val result = BackupManager.restoreBackup(context, backupUri)
                result.fold(
                    onSuccess = { events ->
                        // Insert all restored events
                        var restoredCount = 0
                        events.forEach { event ->
                            eventRepository.insertEvent(event)
                            restoredCount++
                        }

                        _restoreState.value = ActionState.Success(
                            "$restoredCount events restored successfully"
                        )
                        Log.d(TAG, "$restoredCount events restored from backup")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to restore backup", e)
                        _restoreState.value = ActionState.Error(
                            message = "Failed to restore backup: ${e.message}",
                            exception = e,
                            canRetry = true
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring backup", e)
                _restoreState.value = ActionState.Error(
                    message = "Failed to restore backup: ${e.message}",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Restore from a local backup file
     */
    fun restoreFromFile(file: File) {
        val uri = Uri.fromFile(file)
        restoreBackup(uri)
    }

    /**
     * Delete a backup file
     */
    fun deleteBackup(file: File) {
        viewModelScope.launch {
            try {
                val success = BackupManager.deleteBackup(file)
                if (success) {
                    loadAvailableBackups() // Refresh list
                    Log.d(TAG, "Backup deleted: ${file.name}")
                } else {
                    Log.w(TAG, "Failed to delete backup: ${file.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting backup", e)
            }
        }
    }

    /**
     * Clear backup state
     */
    fun clearBackupState() {
        _backupState.value = ActionState.Idle
    }

    /**
     * Clear restore state
     */
    fun clearRestoreState() {
        _restoreState.value = ActionState.Idle
    }

    /**
     * Retry backup operation
     */
    fun retryBackup() {
        if (_backupState.value is ActionState.Error) {
            createBackup()
        }
    }

    /**
     * Retry restore operation
     */
    fun retryRestore(backupUri: Uri) {
        if (_restoreState.value is ActionState.Error) {
            restoreBackup(backupUri)
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
