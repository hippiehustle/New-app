package com.minicount.app

import com.minicount.app.domain.analytics.AnalyticsManager
import org.junit.Assert.*
import org.junit.Test

class AnalyticsTest {

    @Test
    fun `Analytics event names are unique`() {
        val eventNames = listOf(
            AnalyticsManager.Events.EVENT_CREATED,
            AnalyticsManager.Events.EVENT_UPDATED,
            AnalyticsManager.Events.EVENT_DELETED,
            AnalyticsManager.Events.WIDGET_ADDED,
            AnalyticsManager.Events.PREMIUM_PURCHASED,
            AnalyticsManager.Events.BACKUP_EXPORTED,
            AnalyticsManager.Events.ONBOARDING_STARTED
        )

        assertEquals(eventNames.size, eventNames.distinct().size)
    }

    @Test
    fun `Analytics parameter names are unique`() {
        val paramNames = listOf(
            AnalyticsManager.Params.EVENT_ID,
            AnalyticsManager.Params.EVENT_TITLE,
            AnalyticsManager.Params.EVENT_CATEGORY,
            AnalyticsManager.Params.WIDGET_SIZE,
            AnalyticsManager.Params.SHARE_METHOD,
            AnalyticsManager.Params.ERROR_TYPE
        )

        assertEquals(paramNames.size, paramNames.distinct().size)
    }

    @Test
    fun `Analytics event names follow naming convention`() {
        val eventNames = listOf(
            AnalyticsManager.Events.EVENT_CREATED,
            AnalyticsManager.Events.EVENT_UPDATED,
            AnalyticsManager.Events.WIDGET_ADDED,
            AnalyticsManager.Events.PREMIUM_PURCHASED
        )

        eventNames.forEach { eventName ->
            assertTrue("Event name should use snake_case: $eventName",
                eventName.matches(Regex("[a-z_]+")))
        }
    }

    @Test
    fun `Analytics param names follow naming convention`() {
        val paramNames = listOf(
            AnalyticsManager.Params.EVENT_ID,
            AnalyticsManager.Params.WIDGET_SIZE,
            AnalyticsManager.Params.SHARE_METHOD
        )

        paramNames.forEach { paramName ->
            assertTrue("Param name should use snake_case: $paramName",
                paramName.matches(Regex("[a-z_]+")))
        }
    }

    @Test
    fun `All analytics events are defined`() {
        assertNotNull(AnalyticsManager.Events.EVENT_CREATED)
        assertNotNull(AnalyticsManager.Events.EVENT_UPDATED)
        assertNotNull(AnalyticsManager.Events.EVENT_DELETED)
        assertNotNull(AnalyticsManager.Events.WIDGET_ADDED)
        assertNotNull(AnalyticsManager.Events.PREMIUM_PURCHASED)
        assertNotNull(AnalyticsManager.Events.SEARCH_PERFORMED)
        assertNotNull(AnalyticsManager.Events.BACKUP_EXPORTED)
        assertNotNull(AnalyticsManager.Events.ONBOARDING_STARTED)
        assertNotNull(AnalyticsManager.Events.ERROR_OCCURRED)
    }

    @Test
    fun `All analytics params are defined`() {
        assertNotNull(AnalyticsManager.Params.EVENT_ID)
        assertNotNull(AnalyticsManager.Params.EVENT_TITLE)
        assertNotNull(AnalyticsManager.Params.EVENT_CATEGORY)
        assertNotNull(AnalyticsManager.Params.WIDGET_SIZE)
        assertNotNull(AnalyticsManager.Params.SHARE_METHOD)
        assertNotNull(AnalyticsManager.Params.ERROR_TYPE)
        assertNotNull(AnalyticsManager.Params.SCREEN_NAME)
    }
}
