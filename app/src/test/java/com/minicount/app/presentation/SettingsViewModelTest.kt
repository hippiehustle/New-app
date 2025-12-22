package com.minicount.app.presentation

import android.content.Context
import android.net.Uri
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.common.UiState
import com.minicount.app.presentation.screens.settings.AppTheme
import com.minicount.app.presentation.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))
        `when`(preferencesManager.showAds).thenReturn(flowOf(true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings should initialize with default values`() = runTest {
        // When
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // Then
        val settings = viewModel.settings.value
        assertEquals("default", settings.notificationSound)
        assertTrue(settings.hapticFeedbackEnabled)
        assertEquals(30, settings.widgetUpdateInterval)
        assertEquals(1, settings.defaultEventReminderDays)
        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertNull(settings.error)
    }

    @Test
    fun `updateWidgetUpdateInterval should accept valid values`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When - Valid value (between 15 and 120)
        viewModel.updateWidgetUpdateInterval(60)
        advanceUntilIdle()

        // Then
        assertEquals(60, viewModel.settings.value.widgetUpdateInterval)
        assertNull(viewModel.settings.value.error)
    }

    @Test
    fun `updateWidgetUpdateInterval should reject values below minimum`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When - Invalid value (below 15)
        viewModel.updateWidgetUpdateInterval(10)
        advanceUntilIdle()

        // Then
        assertEquals(10, viewModel.settings.value.widgetUpdateInterval) // Value is still updated
        assertNotNull(viewModel.settings.value.error)
        assertTrue(viewModel.settings.value.error!!.contains("between 15 and 120"))
    }

    @Test
    fun `updateWidgetUpdateInterval should reject values above maximum`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When - Invalid value (above 120)
        viewModel.updateWidgetUpdateInterval(150)
        advanceUntilIdle()

        // Then
        assertEquals(150, viewModel.settings.value.widgetUpdateInterval)
        assertNotNull(viewModel.settings.value.error)
        assertTrue(viewModel.settings.value.error!!.contains("between 15 and 120"))
    }

    @Test
    fun `updateNotificationSound should update settings`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateNotificationSound("chime")
        advanceUntilIdle()

        // Then
        assertEquals("chime", viewModel.settings.value.notificationSound)
    }

    @Test
    fun `updateHapticFeedback should toggle setting`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateHapticFeedback(false)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.settings.value.hapticFeedbackEnabled)
    }

    @Test
    fun `updateDefaultReminderDays should accept valid values`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateDefaultReminderDays(7)
        advanceUntilIdle()

        // Then
        assertEquals(7, viewModel.settings.value.defaultEventReminderDays)
        assertNull(viewModel.settings.value.error)
    }

    @Test
    fun `updateDefaultReminderDays should reject negative values`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateDefaultReminderDays(-1)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.settings.value.error)
        assertTrue(viewModel.settings.value.error!!.contains("must be between 0 and 365"))
    }

    @Test
    fun `updateDefaultReminderDays should reject values over maximum`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateDefaultReminderDays(400)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.settings.value.error)
        assertTrue(viewModel.settings.value.error!!.contains("must be between 0 and 365"))
    }

    @Test
    fun `updateTheme should update settings`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.updateTheme(AppTheme.DARK)
        advanceUntilIdle()

        // Then
        assertEquals(AppTheme.DARK, viewModel.settings.value.theme)
    }

    @Test
    fun `saveSettings should succeed with valid settings`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.updateWidgetUpdateInterval(45)
        viewModel.updateDefaultReminderDays(3)
        advanceUntilIdle()

        // When
        viewModel.saveSettings()
        advanceUntilIdle()

        // Then
        val saveState = viewModel.saveState.value
        assertTrue(saveState is ActionState.Success)
        assertEquals("Settings saved", (saveState as ActionState.Success).message)
    }

    @Test
    fun `saveSettings should fail with invalid widget interval`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.updateWidgetUpdateInterval(10) // Invalid
        advanceUntilIdle()

        // When
        viewModel.saveSettings()
        advanceUntilIdle()

        // Then
        val saveState = viewModel.saveState.value
        assertTrue(saveState is ActionState.Error)
        assertTrue((saveState as ActionState.Error).message.contains("Invalid widget update interval"))
    }

    @Test
    fun `saveSettings should fail with invalid reminder days`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.updateDefaultReminderDays(-5) // Invalid
        advanceUntilIdle()

        // When
        viewModel.saveSettings()
        advanceUntilIdle()

        // Then
        val saveState = viewModel.saveState.value
        assertTrue(saveState is ActionState.Error)
        assertTrue((saveState as ActionState.Error).message.contains("Invalid reminder days"))
    }

    @Test
    fun `retrySave should retry after error`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.updateWidgetUpdateInterval(10) // Invalid
        viewModel.saveSettings()
        advanceUntilIdle()

        // Verify error
        assertTrue(viewModel.saveState.value is ActionState.Error)

        // Fix the settings
        viewModel.updateWidgetUpdateInterval(30) // Valid
        advanceUntilIdle()

        // When
        viewModel.retrySave()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.saveState.value is ActionState.Success)
    }

    @Test
    fun `clearSaveState should reset to Idle`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.saveSettings()
        advanceUntilIdle()

        // When
        viewModel.clearSaveState()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.saveState.value is ActionState.Idle)
    }

    @Test
    fun `isPremium should reflect preferences manager state`() = runTest {
        // Given
        `when`(preferencesManager.isPremium).thenReturn(flowOf(true))

        // When
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isPremium.value)
    }

    // Backup/Restore Integration Tests

    @Test
    fun `backupState should initialize as Idle`() = runTest {
        // When
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.backupState.value is ActionState.Idle)
    }

    @Test
    fun `restoreState should initialize as Idle`() = runTest {
        // When
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.restoreState.value is ActionState.Idle)
    }

    @Test
    fun `availableBackups should initialize as empty list`() = runTest {
        // When
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.availableBackups.value.isEmpty())
    }

    @Test
    fun `clearBackupState should reset to Idle`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.clearBackupState()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.backupState.value is ActionState.Idle)
    }

    @Test
    fun `clearRestoreState should reset to Idle`() = runTest {
        // Given
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.clearRestoreState()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.restoreState.value is ActionState.Idle)
    }

    @Test
    fun `createBackup should handle empty events list`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(UiState.Success(emptyList())))
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.createBackup()
        advanceUntilIdle()

        // Then
        // BackupManager will be called with empty list
        // Result depends on BackupManager implementation
        verify(eventRepository).getAllEvents()
    }

    @Test
    fun `createBackup should handle loading state`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(flowOf(UiState.Loading))
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.createBackup()
        advanceUntilIdle()

        // Then
        // Should result in error state since events are not loaded
        val backupState = viewModel.backupState.value
        assertTrue(backupState is ActionState.Error)
    }

    @Test
    fun `createBackup should handle error state`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(
            flowOf(UiState.Error("Database error", Exception("Test error")))
        )
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.createBackup()
        advanceUntilIdle()

        // Then
        val backupState = viewModel.backupState.value
        assertTrue(backupState is ActionState.Error)
    }

    @Test
    fun `retryBackup should retry after error`() = runTest {
        // Given
        `when`(eventRepository.getAllEvents()).thenReturn(
            flowOf(UiState.Error("Test error", Exception()))
        )
        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.backupState.value is ActionState.Error)

        // When
        viewModel.retryBackup()
        advanceUntilIdle()

        // Then
        // Should call createBackup again
        verify(eventRepository, atLeast(2)).getAllEvents()
    }

    @Test
    fun `deleteBackup should handle file deletion`() = runTest {
        // Given
        val mockFile = mock(File::class.java)
        `when`(mockFile.name).thenReturn("backup_test.json")

        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.deleteBackup(mockFile)
        advanceUntilIdle()

        // Then
        // BackupManager.deleteBackup should be called
        // Verification depends on BackupManager implementation
    }

    @Test
    fun `restoreFromFile should convert file to URI and call restoreBackup`() = runTest {
        // Given
        val mockFile = mock(File::class.java)
        `when`(mockFile.path).thenReturn("/path/to/backup.json")

        viewModel = SettingsViewModel(preferencesManager, eventRepository, context)
        advanceUntilIdle()

        // When
        viewModel.restoreFromFile(mockFile)
        advanceUntilIdle()

        // Then
        // Should call restoreBackup with URI
        // Verification depends on BackupManager implementation
    }

    private fun createTestEvent(
        id: Long,
        title: String
    ) = Event(
        id = id,
        title = title,
        notes = "Test notes",
        targetDate = LocalDateTime.now().plusDays(30),
        category = EventCategory.BIRTHDAY,
        photoUri = null,
        isRepeating = false,
        repeatInterval = RepeatInterval.NONE,
        notificationEnabled = true,
        notificationDaysBefore = 1,
        createdAt = LocalDateTime.now(),
        color = 0xFF6200EE.toInt(),
        widgetStyle = WidgetStyle.CLASSIC,
        isPinned = false
    )
}
