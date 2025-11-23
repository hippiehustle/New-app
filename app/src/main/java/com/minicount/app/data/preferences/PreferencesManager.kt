package com.minicount.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val ACTIVE_WIDGETS_COUNT = intPreferencesKey("active_widgets_count")
        val SHOW_ADS = booleanPreferencesKey("show_ads")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.IS_PREMIUM] ?: false
    }

    val activeWidgetsCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.ACTIVE_WIDGETS_COUNT] ?: 0
    }

    val showAds: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val isPremium = preferences[Keys.IS_PREMIUM] ?: false
        if (isPremium) false else (preferences[Keys.SHOW_ADS] ?: true)
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setIsPremium(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_PREMIUM] = isPremium
        }
    }

    suspend fun setActiveWidgetsCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_WIDGETS_COUNT] = count
        }
    }

    suspend fun incrementWidgetCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.ACTIVE_WIDGETS_COUNT] ?: 0
            preferences[Keys.ACTIVE_WIDGETS_COUNT] = current + 1
        }
    }

    suspend fun decrementWidgetCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.ACTIVE_WIDGETS_COUNT] ?: 0
            if (current > 0) {
                preferences[Keys.ACTIVE_WIDGETS_COUNT] = current - 1
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setNotificationPermissionRequested(requested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATION_PERMISSION_REQUESTED] = requested
        }
    }

    suspend fun getIsPremiumSync(): Boolean {
        var isPremium = false
        context.dataStore.edit { preferences ->
            isPremium = preferences[Keys.IS_PREMIUM] ?: false
        }
        return isPremium
    }

    suspend fun getActiveWidgetsCountSync(): Int {
        var count = 0
        context.dataStore.edit { preferences ->
            count = preferences[Keys.ACTIVE_WIDGETS_COUNT] ?: 0
        }
        return count
    }
}
