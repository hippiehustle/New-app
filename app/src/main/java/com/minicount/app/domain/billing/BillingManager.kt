package com.minicount.app.domain.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.minicount.app.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling Google Play Billing operations.
 *
 * This singleton manages:
 * - Connection to Google Play Billing service
 * - Premium purchase flow
 * - Purchase verification and acknowledgment
 * - Premium status synchronization
 *
 * The billing state is exposed via [purchaseState] Flow which ViewModels observe.
 *
 * @property context Application context
 * @property preferencesManager For storing premium status
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)

    /**
     * Current purchase state (Loading, Free, Premium, or Error).
     * ViewModels should observe this to display appropriate UI.
     */
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private lateinit var billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        initializeBillingClient()
    }

    /**
     * Initializes the Google Play Billing client and establishes connection.
     */
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    handlePurchases(purchases)
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
            }
        })
    }

    /**
     * Launches the purchase flow for a product.
     *
     * This presents the Google Play purchase dialog to the user.
     * Results are delivered via [purchaseState] Flow.
     *
     * @param activity Activity to host the purchase flow
     * @param productId Product ID to purchase (default: PREMIUM_SKU)
     */
    fun launchPurchaseFlow(activity: Activity, productId: String = PREMIUM_SKU) {
        val productDetailsParams = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(productDetailsParams))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    /**
     * Queries existing purchases from Google Play.
     * Used on app start and after purchase completion.
     */
    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    /**
     * Processes a list of purchases and updates premium status.
     *
     * @param purchases List of purchases from Google Play
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        val hasPremium = purchases.any { purchase ->
            purchase.products.contains(PREMIUM_SKU) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        scope.launch {
            preferencesManager.setIsPremium(hasPremium)
            _purchaseState.value = if (hasPremium) {
                PurchaseState.Premium
            } else {
                PurchaseState.Free
            }
        }

        // Acknowledge purchases
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }

    /**
     * Acknowledges a purchase to Google Play.
     * Required for non-consumable purchases to complete the transaction.
     *
     * @param purchase Purchase to acknowledge
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryPurchases()
            }
        }
    }

    companion object {
        /** Product ID for premium unlock in-app purchase */
        const val PREMIUM_SKU = "premium_unlock"

        /** Maximum number of events allowed in free version */
        const val FREE_WIDGET_LIMIT = 3
    }
}

/**
 * Represents the current purchase/billing state.
 */
sealed class PurchaseState {
    /** Loading initial purchase state */
    object Loading : PurchaseState()

    /** User has free version */
    object Free : PurchaseState()

    /** User has purchased premium */
    object Premium : PurchaseState()

    /** Error occurred with billing (e.g., service unavailable) */
    data class Error(val message: String) : PurchaseState()
}
