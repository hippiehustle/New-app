package com.minicount.app.presentation.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.Event
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.data.repository.EventRepository
import com.minicount.app.domain.billing.BillingManager
import com.minicount.app.domain.billing.PurchaseState
import com.minicount.app.domain.share.ShareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModelEnhanced @Inject constructor(
    private val eventRepository: EventRepository,
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager,
    private val shareManager: ShareManager,
    @ApplicationContext private val context: Context
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<com.minicount.app.data.local.entity.EventCategory?>(null)
    val selectedCategory: StateFlow<com.minicount.app.data.local.entity.EventCategory?> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(com.minicount.app.presentation.screens.home.SortOption.PINNED_FIRST)
    val sortOption: StateFlow<com.minicount.app.presentation.screens.home.SortOption> = _sortOption.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: com.minicount.app.data.local.entity.EventCategory?) {
        _selectedCategory.value = category
    }

    fun updateSortOption(option: com.minicount.app.presentation.screens.home.SortOption) {
        _sortOption.value = option
    }

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

    fun duplicateEvent(event: Event) {
        viewModelScope.launch {
            val duplicated = event.copy(
                id = 0,
                title = "${event.title} (Copy)",
                isPinned = false
            )
            eventRepository.insertEvent(duplicated)
        }
    }

    fun shareEventAsText(event: Event) {
        val shareIntent = shareManager.shareEventAsText(event)
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share event"))
    }

    fun shareEventAsImage(event: Event) {
        val shareIntent = shareManager.shareEventAsImage(event)
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share countdown image"))
    }

    fun batchDeleteEvents(events: List<Event>) {
        viewModelScope.launch {
            events.forEach { event ->
                eventRepository.deleteEvent(event)
            }
        }
    }
}
