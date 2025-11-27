package com.minicount.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.minicount.app.data.local.MiniCountDatabase
import com.minicount.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create new tables for enhanced features
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS event_photos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventId INTEGER NOT NULL,
                    photoUri TEXT NOT NULL,
                    isPrimary INTEGER NOT NULL DEFAULT 0,
                    `order` INTEGER NOT NULL DEFAULT 0
                )
            """)

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS event_reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventId INTEGER NOT NULL,
                    daysBefore INTEGER NOT NULL,
                    hourOfDay INTEGER NOT NULL DEFAULT 9,
                    minute INTEGER NOT NULL DEFAULT 0,
                    isEnabled INTEGER NOT NULL DEFAULT 1
                )
            """)

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS event_templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    defaultPhotoUri TEXT,
                    color INTEGER NOT NULL,
                    widgetStyle TEXT NOT NULL,
                    isRepeating INTEGER NOT NULL DEFAULT 0,
                    repeatInterval TEXT NOT NULL DEFAULT 'NONE',
                    defaultReminderDays INTEGER NOT NULL DEFAULT 1
                )
            """)

            database.execSQL("""
                CREATE TABLE IF NOT EXISTS widget_config (
                    widgetId INTEGER PRIMARY KEY NOT NULL,
                    eventId INTEGER NOT NULL,
                    showSeconds INTEGER NOT NULL DEFAULT 0,
                    opacity REAL NOT NULL DEFAULT 1.0,
                    customFontSize INTEGER NOT NULL DEFAULT 0,
                    showCategoryIcon INTEGER NOT NULL DEFAULT 1,
                    showEventName INTEGER NOT NULL DEFAULT 1,
                    tapAction TEXT NOT NULL DEFAULT 'OPEN_APP'
                )
            """)
        }
    }

    internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add event history table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS event_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventId INTEGER NOT NULL,
                    eventTitle TEXT NOT NULL,
                    eventCategory TEXT NOT NULL,
                    occurredDate INTEGER NOT NULL,
                    recordedAt INTEGER NOT NULL,
                    notes TEXT NOT NULL DEFAULT ''
                )
            """)
        }
    }

    internal val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Rename description column to notes in events table
            // SQLite doesn't support RENAME COLUMN directly in older versions
            // So we create new table, copy data, and replace
            database.execSQL("""
                CREATE TABLE events_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    targetDate INTEGER NOT NULL,
                    category TEXT NOT NULL DEFAULT 'OTHER',
                    photoUri TEXT,
                    isRepeating INTEGER NOT NULL DEFAULT 0,
                    repeatInterval TEXT NOT NULL DEFAULT 'NONE',
                    notificationEnabled INTEGER NOT NULL DEFAULT 1,
                    notificationDaysBefore INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    color INTEGER NOT NULL DEFAULT -10177041,
                    widgetStyle TEXT NOT NULL DEFAULT 'CLASSIC',
                    isPinned INTEGER NOT NULL DEFAULT 0
                )
            """)

            // Copy data from old table to new (map description to notes)
            database.execSQL("""
                INSERT INTO events_new (id, title, notes, targetDate, category, photoUri,
                    isRepeating, repeatInterval, notificationEnabled, notificationDaysBefore,
                    createdAt, color, widgetStyle, isPinned)
                SELECT id, title, COALESCE(description, ''), targetDate, category, photoUri,
                    isRepeating, repeatInterval, notificationEnabled, notificationDaysBefore,
                    createdAt, color, widgetStyle, isPinned
                FROM events
            """)

            // Drop old table
            database.execSQL("DROP TABLE events")

            // Rename new table
            database.execSQL("ALTER TABLE events_new RENAME TO events")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiniCountDatabase {
        return com.minicount.app.data.local.DatabaseProvider.getDatabase(context)
    }

    @Provides
    fun provideEventDao(database: MiniCountDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    fun provideEventPhotoDao(database: MiniCountDatabase): EventPhotoDao {
        return database.eventPhotoDao()
    }

    @Provides
    fun provideEventReminderDao(database: MiniCountDatabase): EventReminderDao {
        return database.eventReminderDao()
    }

    @Provides
    fun provideEventTemplateDao(database: MiniCountDatabase): EventTemplateDao {
        return database.eventTemplateDao()
    }

    @Provides
    fun provideWidgetConfigDao(database: MiniCountDatabase): WidgetConfigDao {
        return database.widgetConfigDao()
    }

    @Provides
    fun provideEventHistoryDao(database: MiniCountDatabase): EventHistoryDao {
        return database.eventHistoryDao()
    }
}
