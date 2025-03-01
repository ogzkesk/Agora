package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.mvi.ViewEvent

sealed interface MainScreenEvent : ViewEvent {
    data object StartVoiceCalling : MainScreenEvent
}
