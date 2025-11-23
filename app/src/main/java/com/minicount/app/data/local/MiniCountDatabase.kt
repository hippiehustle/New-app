package com.minicount.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minicount.app.data.local.dao.*
import com.minicount.app.data.local.entity.*

@Database(
    entities = [
        Event::class,
        EventPhoto::class,
        EventReminder::class,
        EventTemplate::class,
        WidgetConfig::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MiniCountDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventPhotoDao(): EventPhotoDao
    abstract fun eventReminderDao(): EventReminderDao
    abstract fun eventTemplateDao(): EventTemplateDao
    abstract fun widgetConfigDao(): WidgetConfigDao
}
