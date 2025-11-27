package com.minicount.app.domain.initialization

import android.content.Context
import com.minicount.app.data.local.entity.EventTemplate
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.premade.PremadeContent
import com.minicount.app.data.repository.EventTemplateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles app initialization tasks on first launch
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val templateRepository: EventTemplateRepository
) {
    companion object {
        private const val TAG = "AppInitializer"
    }

    /**
     * Initialize app on first launch
     */
    suspend fun initializeApp() {
        val isFirstLaunch = !preferencesManager.onboardingCompleted.first()

        if (isFirstLaunch) {
            performFirstLaunchSetup()
        }
    }

    /**
     * Perform first launch setup
     */
    private suspend fun performFirstLaunchSetup() {
        // 1. Initialize premade templates
        initializeTemplates()

        // 2. Set default preferences
        setDefaultPreferences()

        // 3. Any other initialization tasks
        // (e.g., create default categories, set up notifications, etc.)
    }

    /**
     * Initialize premade event templates in database
     */
    private suspend fun initializeTemplates() {
        try {
            // Check if templates already exist
            val existingTemplates = templateRepository.getAllTemplates().first()

            if (existingTemplates.isEmpty()) {
                // Insert all premade templates
                PremadeContent.EVENT_TEMPLATES.forEach { template ->
                    templateRepository.insertTemplate(template)
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash app
            android.util.Log.e(TAG, "Failed to initialize templates", e)
        }
    }

    /**
     * Set default preferences
     */
    private suspend fun setDefaultPreferences() {
        // Set default theme
        // preferencesManager.setTheme("material_purple")

        // Set default notification preferences
        // Already handled by defaults in PreferencesManager

        // Mark initialization complete
        // Will be marked when onboarding completes
    }

    /**
     * Re-initialize templates (for updates)
     */
    suspend fun refreshTemplates() {
        try {
            // Get existing templates
            val existing = templateRepository.getAllTemplates().first()
            val existingIds = existing.map { it.id }.toSet()

            // Add any new templates that don't exist
            PremadeContent.EVENT_TEMPLATES.forEach { template ->
                if (template.id !in existingIds) {
                    templateRepository.insertTemplate(template)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to refresh templates", e)
        }
    }

    /**
     * Clear all data (for testing/debugging)
     */
    suspend fun clearAllData() {
        // Delete all templates
        val templates = templateRepository.getAllTemplates().first()
        templates.forEach { templateRepository.deleteTemplate(it) }

        // Reset preferences
        preferencesManager.setOnboardingCompleted(false)
        preferencesManager.setIsPremium(false)
    }
}
