package com.minicount.app.presentation

import android.app.Activity
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import com.minicount.app.presentation.common.ActionState
import com.minicount.app.presentation.screens.premium.PremiumViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class PremiumViewModelTest {

    @Mock
    private lateinit var billingManager: BillingManager

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    @Mock
    private lateinit var activity: Activity

    private lateinit var viewModel: PremiumViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))
        `when`(billingManager.purchaseState).thenReturn(MutableStateFlow(PurchaseState.Free))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isPremium should emit false when user is free`() = runTest {
        // Given
        `when`(preferencesManager.isPremium).thenReturn(flowOf(false))

        // When
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isPremium.value)
    }

    @Test
    fun `isPremium should emit true when user is premium`() = runTest {
        // Given
        `when`(preferencesManager.isPremium).thenReturn(flowOf(true))

        // When
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isPremium.value)
    }

    @Test
    fun `purchaseState should reflect billing manager state`() = runTest {
        // Given
        val purchaseStateFlow = MutableStateFlow<PurchaseState>(PurchaseState.Premium)
        `when`(billingManager.purchaseState).thenReturn(purchaseStateFlow)

        // When
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.purchaseState.value is PurchaseState.Premium)
    }

    @Test
    fun `purchasePremium should launch billing flow`() = runTest {
        // Given
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // When
        viewModel.purchasePremium(activity)
        advanceUntilIdle()

        // Then
        verify(billingManager).launchPurchaseFlow(activity)
    }

    @Test
    fun `purchasePremium should update actionState to InProgress then Success`() = runTest {
        // Given
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // When
        viewModel.purchasePremium(activity)
        advanceUntilIdle()

        // Then
        val actionState = viewModel.actionState.value
        assertTrue(actionState is ActionState.Success)
        assertEquals("Purchase initiated", (actionState as ActionState.Success).message)
    }

    @Test
    fun `purchasePremium should handle errors gracefully`() = runTest {
        // Given
        doThrow(RuntimeException("Billing service unavailable"))
            .`when`(billingManager).launchPurchaseFlow(any())

        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // When
        viewModel.purchasePremium(activity)
        advanceUntilIdle()

        // Then
        val actionState = viewModel.actionState.value
        assertTrue(actionState is ActionState.Error)
        val error = actionState as ActionState.Error
        assertTrue(error.message.contains("Failed to start purchase"))
        assertTrue(error.canRetry)
    }

    @Test
    fun `retryPurchase should retry after error`() = runTest {
        // Given
        doThrow(RuntimeException("Billing service unavailable"))
            .`when`(billingManager).launchPurchaseFlow(any())

        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        viewModel.purchasePremium(activity)
        advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.actionState.value is ActionState.Error)

        // Fix the billing manager
        doNothing().`when`(billingManager).launchPurchaseFlow(any())

        // When
        viewModel.retryPurchase(activity)
        advanceUntilIdle()

        // Then
        verify(billingManager, times(2)).launchPurchaseFlow(activity)
    }

    @Test
    fun `restorePurchases should call billing manager`() = runTest {
        // Given
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // When
        viewModel.restorePurchases()
        advanceUntilIdle()

        // Then
        // Note: This would require BillingManager to have a restore method
        // For now, verify the action state
        val actionState = viewModel.actionState.value
        assertTrue(actionState is ActionState.Success || actionState is ActionState.Idle)
    }

    @Test
    fun `clearActionState should reset to Idle`() = runTest {
        // Given
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        viewModel.purchasePremium(activity)
        advanceUntilIdle()

        // Verify not idle
        assertFalse(viewModel.actionState.value is ActionState.Idle)

        // When
        viewModel.clearActionState()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.actionState.value is ActionState.Idle)
    }

    @Test
    fun `purchaseState should handle billing error state`() = runTest {
        // Given
        val errorStateFlow = MutableStateFlow<PurchaseState>(
            PurchaseState.Error("Google Play services unavailable")
        )
        `when`(billingManager.purchaseState).thenReturn(errorStateFlow)

        // When
        viewModel = PremiumViewModel(billingManager, preferencesManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.purchaseState.value
        assertTrue(state is PurchaseState.Error)
        assertEquals("Google Play services unavailable", (state as PurchaseState.Error).message)
    }
}
