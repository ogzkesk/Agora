package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.mvi.ViewEvent

sealed interface MainScreenEvent : ViewEvent {
    data object StartVideoCalling : MainScreenEvent
    data object StartVoiceCalling : MainScreenEvent
    data object ResetErrorState : MainScreenEvent
    data class ToggleTemporaryToken(val value: Boolean) : MainScreenEvent
    data class ChannelNameChangedEvent(val value: String) : MainScreenEvent
    data class TempTokenChangedEvent(val value: String) : MainScreenEvent
}
