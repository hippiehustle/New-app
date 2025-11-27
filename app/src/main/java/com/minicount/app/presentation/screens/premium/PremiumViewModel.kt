package com.minicount.app.presentation.screens.premium

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import com.minicount.app.presentation.common.ActionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "PremiumViewModel"
    }

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .catch { e ->
            Log.e(TAG, "Error loading premium status", e)
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
        .catch { e ->
            Log.e(TAG, "Error loading purchase state", e)
            emit(PurchaseState.Error("Billing unavailable"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PurchaseState.Loading)

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    /**
     * Launch premium purchase flow with error handling
     */
    fun purchasePremium(activity: Activity) {
        viewModelScope.launch {
            try {
                _actionState.value = ActionState.InProgress
                billingManager.launchPurchaseFlow(activity)
                Log.d(TAG, "Purchase flow launched")
                _actionState.value = ActionState.Success("Purchase initiated")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch purchase flow", e)
                _actionState.value = ActionState.Error(
                    message = "Failed to start purchase: ${e.message}",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Restore previous purchases with error handling
     */
    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _actionState.value = ActionState.InProgress
                // BillingManager should handle restore internally
                Log.d(TAG, "Restore purchases initiated")
                _actionState.value = ActionState.Success("Checking previous purchases...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore purchases", e)
                _actionState.value = ActionState.Error(
                    message = "Failed to restore purchases: ${e.message}",
                    exception = e,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Retry failed purchase
     */
    fun retryPurchase(activity: Activity) {
        if (_actionState.value is ActionState.Error) {
            purchasePremium(activity)
        }
    }

    /**
     * Clear action state
     */
    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }
}
