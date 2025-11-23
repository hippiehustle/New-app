package com.minicount.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager
) : ViewModel() {

    val events: StateFlow<List<Event>> = eventRepository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventCount: StateFlow<Int> = eventRepository.getEventCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PurchaseState.Loading)

    val canAddMoreEvents: StateFlow<Boolean> = combine(
        eventCount,
        isPremium
    ) { count, premium ->
        premium || count < BillingManager.FREE_WIDGET_LIMIT
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
        }
    }

    fun togglePinEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event.copy(isPinned = !event.isPinned))
        }
    }
}
