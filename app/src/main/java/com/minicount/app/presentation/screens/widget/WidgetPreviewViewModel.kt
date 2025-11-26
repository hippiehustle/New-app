package com.minicount.app.presentation.screens.widget

import androidx.lifecycle.ViewModel
import com.minicount.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WidgetPreviewViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    fun getEvent(eventId: Long) = eventRepository.getEventById(eventId)
}
