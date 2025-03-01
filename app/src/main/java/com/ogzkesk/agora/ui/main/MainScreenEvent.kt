package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.mvi.ViewEvent

sealed interface MainScreenEvent : ViewEvent {
    data object StartVoiceCalling : MainScreenEvent
    data class ToggleTemporaryToken(val value: Boolean) : MainScreenEvent
    data class ChannelNameChangedEvent(val value: String) : MainScreenEvent
}
