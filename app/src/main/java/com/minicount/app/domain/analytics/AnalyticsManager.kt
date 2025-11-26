package com.minicount.app.domain.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics manager for tracking user events and behavior
 * Ready for integration with Firebase Analytics or other analytics providers
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AnalyticsManager"
        private const val ENABLED = true // Set to false to disable analytics
    }

    // Event names
    object Events {
        const val EVENT_CREATED = "event_created"
        const val EVENT_UPDATED = "event_updated"
        const val EVENT_DELETED = "event_deleted"
        const val EVENT_DUPLICATED = "event_duplicated"
        const val EVENT_SHARED = "event_shared"
        const val EVENT_VIEWED = "event_viewed"

        const val WIDGET_ADDED = "widget_added"
        const val WIDGET_REMOVED = "widget_removed"
        const val WIDGET_UPDATED = "widget_updated"

        const val PREMIUM_PURCHASED = "premium_purchased"
        const val PREMIUM_VIEW_CLICKED = "premium_view_clicked"
        const val AD_VIEWED = "ad_viewed"

        const val SEARCH_PERFORMED = "search_performed"
        const val FILTER_APPLIED = "filter_applied"
        const val SORT_CHANGED = "sort_changed"

        const val BACKUP_EXPORTED = "backup_exported"
        const val BACKUP_IMPORTED = "backup_imported"

        const val NOTIFICATION_SENT = "notification_sent"
        const val NOTIFICATION_CLICKED = "notification_clicked"

        const val ONBOARDING_STARTED = "onboarding_started"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val ONBOARDING_SKIPPED = "onboarding_skipped"

        const val SETTINGS_VIEWED = "settings_viewed"
        const val STATISTICS_VIEWED = "statistics_viewed"
        const val CALENDAR_VIEWED = "calendar_viewed"
        const val HISTORY_VIEWED = "history_viewed"

        const val APP_OPENED = "app_opened"
        const val APP_BACKGROUNDED = "app_backgrounded"

        const val ERROR_OCCURRED = "error_occurred"
    }

    // Parameter names
    object Params {
        const val EVENT_ID = "event_id"
        const val EVENT_TITLE = "event_title"
        const val EVENT_CATEGORY = "event_category"
        const val IS_REPEATING = "is_repeating"
        const val HAS_PHOTO = "has_photo"

        const val WIDGET_SIZE = "widget_size"
        const val WIDGET_ID = "widget_id"

        const val SHARE_METHOD = "share_method"
        const val SEARCH_QUERY = "search_query"
        const val FILTER_CATEGORY = "filter_category"
        const val SORT_OPTION = "sort_option"

        const val ERROR_TYPE = "error_type"
        const val ERROR_MESSAGE = "error_message"

        const val PURCHASE_TYPE = "purchase_type"
        const val PRICE = "price"

        const val SCREEN_NAME = "screen_name"
        const val ACTION_TYPE = "action_type"
    }

    /**
     * Log an event with parameters
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        if (!ENABLED) return

        // For now, just log to console
        // Replace with Firebase Analytics or your preferred analytics provider
        val paramsString = params.entries.joinToString { "${it.key}=${it.value}" }
        Log.d(TAG, "Event: $eventName, Params: {$paramsString}")

        // TODO: Replace with actual analytics implementation
        // firebaseAnalytics.logEvent(eventName, Bundle().apply {
        //     params.forEach { (key, value) ->
        //         when (value) {
        //             is String -> putString(key, value)
        //             is Int -> putInt(key, value)
        //             is Long -> putLong(key, value)
        //             is Boolean -> putBoolean(key, value)
        //             is Double -> putDouble(key, value)
        //         }
        //     }
        // })
    }

    /**
     * Track screen view
     */
    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf(Params.SCREEN_NAME to screenName))
    }

    // Convenience methods for common events

    fun logEventCreated(event: Event) {
        logEvent(Events.EVENT_CREATED, mapOf(
            Params.EVENT_CATEGORY to event.category.name,
            Params.IS_REPEATING to event.isRepeating,
            Params.HAS_PHOTO to (event.photoUri != null)
        ))
    }

    fun logEventUpdated(event: Event) {
        logEvent(Events.EVENT_UPDATED, mapOf(
            Params.EVENT_ID to event.id,
            Params.EVENT_CATEGORY to event.category.name
        ))
    }

    fun logEventDeleted(eventCategory: EventCategory) {
        logEvent(Events.EVENT_DELETED, mapOf(
            Params.EVENT_CATEGORY to eventCategory.name
        ))
    }

    fun logEventDuplicated(event: Event) {
        logEvent(Events.EVENT_DUPLICATED, mapOf(
            Params.EVENT_CATEGORY to event.category.name
        ))
    }

    fun logEventShared(shareMethod: String) {
        logEvent(Events.EVENT_SHARED, mapOf(
            Params.SHARE_METHOD to shareMethod
        ))
    }

    fun logWidgetAdded(widgetSize: String) {
        logEvent(Events.WIDGET_ADDED, mapOf(
            Params.WIDGET_SIZE to widgetSize
        ))
    }

    fun logWidgetRemoved(widgetId: Int) {
        logEvent(Events.WIDGET_REMOVED, mapOf(
            Params.WIDGET_ID to widgetId
        ))
    }

    fun logPremiumPurchased(price: String) {
        logEvent(Events.PREMIUM_PURCHASED, mapOf(
            Params.PRICE to price
        ))
    }

    fun logPremiumViewClicked() {
        logEvent(Events.PREMIUM_VIEW_CLICKED)
    }

    fun logSearchPerformed(query: String) {
        logEvent(Events.SEARCH_PERFORMED, mapOf(
            Params.SEARCH_QUERY to query
        ))
    }

    fun logFilterApplied(category: String) {
        logEvent(Events.FILTER_APPLIED, mapOf(
            Params.FILTER_CATEGORY to category
        ))
    }

    fun logSortChanged(sortOption: String) {
        logEvent(Events.SORT_CHANGED, mapOf(
            Params.SORT_OPTION to sortOption
        ))
    }

    fun logBackupExported() {
        logEvent(Events.BACKUP_EXPORTED)
    }

    fun logBackupImported() {
        logEvent(Events.BACKUP_IMPORTED)
    }

    fun logOnboardingStarted() {
        logEvent(Events.ONBOARDING_STARTED)
    }

    fun logOnboardingCompleted() {
        logEvent(Events.ONBOARDING_COMPLETED)
    }

    fun logOnboardingSkipped() {
        logEvent(Events.ONBOARDING_SKIPPED)
    }

    fun logError(errorType: String, errorMessage: String) {
        logEvent(Events.ERROR_OCCURRED, mapOf(
            Params.ERROR_TYPE to errorType,
            Params.ERROR_MESSAGE to errorMessage
        ))
    }

    /**
     * Set user properties
     */
    fun setUserProperty(name: String, value: String) {
        if (!ENABLED) return

        Log.d(TAG, "User Property: $name = $value")
        // TODO: Replace with actual analytics implementation
        // firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Track user premium status
     */
    fun setUserPremiumStatus(isPremium: Boolean) {
        setUserProperty("is_premium", isPremium.toString())
    }

    /**
     * Track total event count
     */
    fun setUserEventCount(count: Int) {
        setUserProperty("event_count", count.toString())
    }
}
