package com.ogzkesk.agora.ui.call

import com.ogzkesk.agora.mvi.ViewEvent

sealed interface VoiceCallScreenEvent : ViewEvent {
    data object EndCall : VoiceCallScreenEvent
    data class RemoteVolumeChange(val id: Int, val volume: Int) : VoiceCallScreenEvent
    data class LocalVolumeChange(val volume: Int) : VoiceCallScreenEvent
    data class ToggleMuteLocal(val value: Boolean) : VoiceCallScreenEvent
    data class ToggleMuteRemote(val id: Int, val value: Boolean) : VoiceCallScreenEvent
}
