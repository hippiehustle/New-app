package com.minicount.app.presentation

import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.presentation.common.ActionState
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings should initialize with default values`() = runTest {
        // When
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
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
        viewModel = SettingsViewModel(preferencesManager)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isPremium.value)
    }
}
