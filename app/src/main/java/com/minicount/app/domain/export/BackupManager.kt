package com.minicount.app.domain.export

import android.content.Context
import android.net.Uri
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.repository.EventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class EventExport(
    val id: Long,
    val title: String,
    val description: String,
    val targetDateString: String,
    val categoryName: String,
    val photoUri: String?,
    val isRepeating: Boolean,
    val repeatIntervalName: String,
    val notificationEnabled: Boolean,
    val notificationDaysBefore: Int,
    val color: Int,
    val widgetStyleName: String,
    val isPinned: Boolean
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val events: List<EventExport>
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(): String {
        val events = eventRepository.getAllEvents().first()
        val exportEvents = events.map { it.toExport() }

        val backup = BackupData(
            version = 1,
            exportDate = LocalDateTime.now().toString(),
            events = exportEvents
        )

        return json.encodeToString(backup)
    }

    suspend fun exportToFile(uri: Uri): Result<Unit> {
        return try {
            val jsonString = exportToJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)
            var imported = 0

            backup.events.forEach { exportEvent ->
                val event = exportEvent.toEvent()
                eventRepository.insertEvent(event)
                imported++
            }

            Result.success(imported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromFile(uri: Uri): Result<Int> {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: return Result.failure(Exception("Could not read file"))

            importFromJson(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Event.toExport() = EventExport(
        id = id,
        title = title,
        description = description,
        targetDateString = targetDate.toString(),
        categoryName = category.name,
        photoUri = photoUri,
        isRepeating = isRepeating,
        repeatIntervalName = repeatInterval.name,
        notificationEnabled = notificationEnabled,
        notificationDaysBefore = notificationDaysBefore,
        color = color,
        widgetStyleName = widgetStyle.name,
        isPinned = isPinned
    )

    private fun EventExport.toEvent() = Event(
        id = 0, // Let database auto-generate
        title = title,
        description = description,
        targetDate = LocalDateTime.parse(targetDateString),
        category = com.minicount.app.data.local.entity.EventCategory.valueOf(categoryName),
        photoUri = photoUri,
        isRepeating = isRepeating,
        repeatInterval = com.minicount.app.data.local.entity.RepeatInterval.valueOf(repeatIntervalName),
        notificationEnabled = notificationEnabled,
        notificationDaysBefore = notificationDaysBefore,
        color = color,
        widgetStyle = com.minicount.app.data.local.entity.WidgetStyle.valueOf(widgetStyleName),
        isPinned = isPinned
    )
}
