package com.minicount.app.data.local

import androidx.room.TypeConverter
import com.minicount.app.data.local.entity.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromEventCategory(value: EventCategory): String {
        return value.name
    }

    @TypeConverter
    fun toEventCategory(value: String): EventCategory {
        return EventCategory.valueOf(value)
    }

    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval {
        return RepeatInterval.valueOf(value)
    }

    @TypeConverter
    fun fromWidgetStyle(value: WidgetStyle): String {
        return value.name
    }

    @TypeConverter
    fun toWidgetStyle(value: String): WidgetStyle {
        return WidgetStyle.valueOf(value)
    }

    @TypeConverter
    fun fromWidgetTapAction(value: WidgetTapAction): String {
        return value.name
    }

    @TypeConverter
    fun toWidgetTapAction(value: String): WidgetTapAction {
        return WidgetTapAction.valueOf(value)
    }
}
