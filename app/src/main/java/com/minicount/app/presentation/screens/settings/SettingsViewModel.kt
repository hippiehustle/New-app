package com.minicount.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showAds: StateFlow<Boolean> = preferencesManager.showAds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    fun updateNotificationSound(sound: String) {
        _settings.value = _settings.value.copy(notificationSound = sound)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled)
    }

    fun updateWidgetUpdateInterval(minutes: Int) {
        _settings.value = _settings.value.copy(widgetUpdateInterval = minutes)
    }

    fun updateDefaultEventReminder(days: Int) {
        _settings.value = _settings.value.copy(defaultEventReminderDays = days)
    }

    fun updateTheme(theme: AppTheme) {
        _settings.value = _settings.value.copy(theme = theme)
    }
}

data class SettingsState(
    val notificationSound: String = "default",
    val hapticFeedbackEnabled: Boolean = true,
    val widgetUpdateInterval: Int = 30,
    val defaultEventReminderDays: Int = 1,
    val theme: AppTheme = AppTheme.SYSTEM
)

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}
