package com.minicount.app.domain.premium

import android.content.Context
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.premade.PremadeContent
import com.minicount.app.data.local.entity.WidgetStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages premium features and purchases
 */
@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val _purchasedWidgetStyles = MutableStateFlow<Set<String>>(emptySet())
    val purchasedWidgetStyles: Flow<Set<String>> = _purchasedWidgetStyles.asStateFlow()

    private val _purchasedThemes = MutableStateFlow<Set<String>>(emptySet())
    val purchasedThemes: Flow<Set<String>> = _purchasedThemes.asStateFlow()

    /**
     * Check if user has premium access
     */
    suspend fun isPremium(): Boolean {
        return preferencesManager.isPremium.first()
    }

    /**
     * Check if a widget style is unlocked
     */
    suspend fun isWidgetStyleUnlocked(style: WidgetStyle): Boolean {
        // Free styles are always unlocked
        if (!style.isPremium) return true

        // Premium users get all styles
        if (isPremium()) return true

        // Check if individually purchased
        return _purchasedWidgetStyles.value.contains(style.name)
    }

    /**
     * Check if a color theme is unlocked
     */
    suspend fun isThemeUnlocked(themeId: String): Boolean {
        // Free themes
        val freeThemeIds = PremadeContent.FREE_THEMES.map { it.id }
        if (themeId in freeThemeIds) return true

        // Premium users get all themes
        if (isPremium()) return true

        // Check if individually purchased
        return _purchasedThemes.value.contains(themeId)
    }

    /**
     * Unlock a widget style (after purchase)
     */
    suspend fun unlockWidgetStyle(style: WidgetStyle) {
        val current = _purchasedWidgetStyles.value.toMutableSet()
        current.add(style.name)
        _purchasedWidgetStyles.value = current

        // Persist to preferences
        saveWidgetStyles(current)
    }

    /**
     * Unlock a theme (after purchase)
     */
    suspend fun unlockTheme(themeId: String) {
        val current = _purchasedThemes.value.toMutableSet()
        current.add(themeId)
        _purchasedThemes.value = current

        // Persist to preferences
        saveThemes(current)
    }

    /**
     * Unlock premium (after purchase)
     */
    suspend fun unlockPremium() {
        preferencesManager.setIsPremium(true)
    }

    /**
     * Get all unlocked widget styles
     */
    suspend fun getUnlockedWidgetStyles(): List<WidgetStyle> {
        val allStyles = WidgetStyle.values().toList()

        return if (isPremium()) {
            allStyles
        } else {
            val purchasedNames = _purchasedWidgetStyles.value
            allStyles.filter { !it.isPremium || it.name in purchasedNames }
        }
    }

    /**
     * Get all unlocked themes
     */
    suspend fun getUnlockedThemes(): List<PremadeContent.ColorTheme> {
        val allThemes = PremadeContent.FREE_THEMES + PremadeContent.PREMIUM_THEMES

        return if (isPremium()) {
            allThemes
        } else {
            val purchased = _purchasedThemes.value
            allThemes.filter { !it.isPremium || it.id in purchased }
        }
    }

    /**
     * Get locked widget styles
     */
    suspend fun getLockedWidgetStyles(): List<WidgetStyle> {
        return if (isPremium()) {
            emptyList()
        } else {
            val purchasedNames = _purchasedWidgetStyles.value
            WidgetStyle.getPremiumStyles().filter { it.name !in purchasedNames }
        }
    }

    /**
     * Get locked themes
     */
    suspend fun getLockedThemes(): List<PremadeContent.ColorTheme> {
        return if (isPremium()) {
            emptyList()
        } else {
            val purchased = _purchasedThemes.value
            PremadeContent.PREMIUM_THEMES.filter { it.id !in purchased }
        }
    }

    /**
     * Calculate total savings from bundles
     */
    fun calculateBundleSavings(bundleType: BundleType): Long {
        return when (bundleType) {
            BundleType.WIDGET_BUNDLE -> {
                val individualTotal = PremadeContent.Pricing.WIDGET_STYLE * 10
                individualTotal - PremadeContent.Pricing.WIDGET_BUNDLE
            }
            BundleType.THEME_BUNDLE -> {
                val individualTotal = PremadeContent.Pricing.COLOR_THEME * 12
                individualTotal - PremadeContent.Pricing.THEME_BUNDLE
            }
            BundleType.ULTIMATE_BUNDLE -> {
                val widgetTotal = PremadeContent.Pricing.WIDGET_STYLE * 10
                val themeTotal = PremadeContent.Pricing.COLOR_THEME * 12
                val premiumPrice = PremadeContent.Pricing.PREMIUM_UNLIMITED
                (widgetTotal + themeTotal + premiumPrice) - PremadeContent.Pricing.ULTIMATE_BUNDLE
            }
        }
    }

    /**
     * Format savings as currency
     */
    fun formatSavings(savingsMicros: Long): String {
        val dollars = savingsMicros / 1_000_000
        val cents = (savingsMicros % 1_000_000) / 10_000
        return "$$dollars.${cents.toString().padStart(2, '0')}"
    }

    /**
     * Persist widget styles to preferences
     */
    private suspend fun saveWidgetStyles(styles: Set<String>) {
        // TODO: Implement persistence with DataStore or SharedPreferences
        // For now, just keep in memory
    }

    /**
     * Persist themes to preferences
     */
    private suspend fun saveThemes(themes: Set<String>) {
        // TODO: Implement persistence with DataStore or SharedPreferences
        // For now, just keep in memory
    }

    /**
     * Load purchased items from persistence
     */
    suspend fun loadPurchasedItems() {
        // TODO: Load from DataStore or SharedPreferences
        // For now, empty
    }

    enum class BundleType {
        WIDGET_BUNDLE,
        THEME_BUNDLE,
        ULTIMATE_BUNDLE
    }
}
