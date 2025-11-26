package com.minicount.app.domain.shortcuts

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.minicount.app.R
import com.minicount.app.data.local.entity.Event
import com.minicount.app.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages dynamic app shortcuts for quick access to features
 */
@Singleton
class AppShortcutsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SHORTCUT_ADD_EVENT = "shortcut_add_event"
        private const val SHORTCUT_VIEW_EVENTS = "shortcut_view_events"
        private const val SHORTCUT_RECENT_EVENT_PREFIX = "shortcut_recent_event_"
        private const val MAX_SHORTCUTS = 4
    }

    /**
     * Update all app shortcuts
     */
    fun updateShortcuts(recentEvents: List<Event> = emptyList()) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return
        }

        val shortcuts = mutableListOf<ShortcutInfoCompat>()

        // Add "Add Event" shortcut
        shortcuts.add(createAddEventShortcut())

        // Add shortcuts for recent events (up to 3)
        recentEvents.take(3).forEachIndexed { index, event ->
            shortcuts.add(createEventShortcut(event, index))
        }

        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    /**
     * Create shortcut for adding a new event
     */
    private fun createAddEventShortcut(): ShortcutInfoCompat {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "add_event")
        }

        return ShortcutInfoCompat.Builder(context, SHORTCUT_ADD_EVENT)
            .setShortLabel("Add Event")
            .setLongLabel("Add New Countdown Event")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground))
            .setIntent(intent)
            .setRank(0)
            .build()
    }

    /**
     * Create shortcut for a specific event
     */
    private fun createEventShortcut(event: Event, rank: Int): ShortcutInfoCompat {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("eventId", event.id)
        }

        return ShortcutInfoCompat.Builder(context, "$SHORTCUT_RECENT_EVENT_PREFIX${event.id}")
            .setShortLabel(event.title)
            .setLongLabel("${event.category.icon} ${event.title}")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground))
            .setIntent(intent)
            .setRank(rank + 1)
            .build()
    }

    /**
     * Remove all dynamic shortcuts
     */
    fun removeAllShortcuts() {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }

    /**
     * Remove a specific shortcut
     */
    fun removeShortcut(shortcutId: String) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId))
    }

    /**
     * Remove shortcut for a specific event
     */
    fun removeEventShortcut(eventId: Long) {
        removeShortcut("$SHORTCUT_RECENT_EVENT_PREFIX$eventId")
    }

    /**
     * Push a shortcut to be pinned on the launcher
     */
    fun requestPinShortcut(event: Event): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val shortcut = createEventShortcut(event, 0)

        return ShortcutManagerCompat.requestPinShortcut(
            context,
            shortcut,
            null
        )
    }

    /**
     * Report shortcut usage (helps Android prioritize shortcuts)
     */
    fun reportShortcutUsed(shortcutId: String) {
        ShortcutManagerCompat.reportShortcutUsed(context, shortcutId)
    }

    /**
     * Get maximum number of shortcuts allowed
     */
    fun getMaxShortcutCount(): Int {
        return ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
    }

    /**
     * Check if shortcuts are supported
     */
    fun areShortcutsSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    }
}
