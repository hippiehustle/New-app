package com.minicount.app.domain.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.minicount.app.data.local.entity.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {
    private const val TAG = "BackupManager"
    private const val BACKUP_DIR = "backups"
    private const val BACKUP_FILE_PREFIX = "minicount_backup_"
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    suspend fun createBackup(
        context: Context,
        events: List<Event>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "$BACKUP_FILE_PREFIX$timestamp.json")
            
            val backupData = BackupData(
                version = 1,
                timestamp = System.currentTimeMillis(),
                eventCount = events.size,
                events = events.map { it.toSerializable() }
            )
            
            val jsonString = json.encodeToString(backupData)
            backupFile.writeText(jsonString)
            
            Log.d(TAG, "Backup created: ${backupFile.absolutePath}")
            Result.success(Uri.fromFile(backupFile))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create backup", e)
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(
        context: Context,
        backupUri: Uri
    ): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(backupUri)
                ?: return@withContext Result.failure(Exception("Cannot open backup file"))
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            val events = backupData.events.map { it.toEvent() }
            
            Log.d(TAG, "Backup restored: ${events.size} events")
            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore backup", e)
            Result.failure(e)
        }
    }
    
    fun listBackups(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR)
        if (!backupDir.exists()) return emptyList()
        
        return backupDir.listFiles { file ->
            file.name.startsWith(BACKUP_FILE_PREFIX) && file.extension == "json"
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun deleteBackup(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup", e)
            false
        }
    }
}

@kotlinx.serialization.Serializable
data class BackupData(
    val version: Int,
    val timestamp: Long,
    val eventCount: Int,
    val events: List<SerializableEvent>
)

@kotlinx.serialization.Serializable
data class SerializableEvent(
    val id: Long,
    val title: String,
    val notes: String,
    val targetDate: String,
    val category: String,
    val isPinned: Boolean,
    val isRepeating: Boolean
)

private fun Event.toSerializable() = SerializableEvent(
    id = id,
    title = title,
    notes = notes,
    targetDate = targetDate.toString(),
    category = category.name,
    isPinned = isPinned,
    isRepeating = isRepeating
)

private fun SerializableEvent.toEvent() = Event(
    id = 0,
    title = title,
    notes = notes,
    targetDate = java.time.LocalDateTime.parse(targetDate),
    category = com.minicount.app.data.local.entity.EventCategory.valueOf(category),
    isPinned = isPinned,
    isRepeating = isRepeating
)
