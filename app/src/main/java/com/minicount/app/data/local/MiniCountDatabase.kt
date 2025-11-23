package com.minicount.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minicount.app.data.local.dao.EventDao
import com.minicount.app.data.local.entity.Event

@Database(
    entities = [Event::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MiniCountDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
