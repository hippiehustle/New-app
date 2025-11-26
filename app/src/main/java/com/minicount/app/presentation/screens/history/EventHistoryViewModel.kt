package com.minicount.app.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicount.app.data.local.entity.EventHistory
import com.minicount.app.data.repository.EventHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EventHistoryViewModel @Inject constructor(
    private val eventHistoryRepository: EventHistoryRepository
) : ViewModel() {

    val allHistory: StateFlow<List<EventHistory>> = eventHistoryRepository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHistoryForEvent(eventId: Long) = eventHistoryRepository.getHistoryForEvent(eventId)
}
