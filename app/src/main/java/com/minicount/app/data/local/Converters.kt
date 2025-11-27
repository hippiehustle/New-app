package com.minicount.app.data.local

import android.util.Log
import androidx.room.TypeConverter
import com.minicount.app.data.local.entity.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverters for custom data types with error handling
 */
class Converters {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    companion object {
        private const val TAG = "Converters"
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return try {
            value?.let { LocalDateTime.parse(it, formatter) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse LocalDateTime: $value", e)
            null
        }
    }

    @TypeConverter
    fun fromEventCategory(value: EventCategory): String {
        return value.name
    }

    @TypeConverter
    fun toEventCategory(value: String): EventCategory {
        return try {
            EventCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unknown EventCategory: $value, falling back to OTHER", e)
            EventCategory.OTHER
        }
    }

    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval {
        return try {
            RepeatInterval.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unknown RepeatInterval: $value, falling back to NONE", e)
            RepeatInterval.NONE
        }
    }

    @TypeConverter
    fun fromWidgetStyle(value: WidgetStyle): String {
        return value.name
    }

    @TypeConverter
    fun toWidgetStyle(value: String): WidgetStyle {
        return try {
            WidgetStyle.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unknown WidgetStyle: $value, falling back to CLASSIC", e)
            WidgetStyle.CLASSIC
        }
    }

    @TypeConverter
    fun fromWidgetTapAction(value: WidgetTapAction): String {
        return value.name
    }

    @TypeConverter
    fun toWidgetTapAction(value: String): WidgetTapAction {
        return try {
            WidgetTapAction.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unknown WidgetTapAction: $value, falling back to OPEN_APP", e)
            WidgetTapAction.OPEN_APP
        }
    }
}
