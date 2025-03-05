package com.ogzkesk.agora.ui.voice

import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode
import com.ogzkesk.agora.mvi.ViewEvent

sealed interface VoiceCallScreenEvent : ViewEvent {
    data object EndCall : VoiceCallScreenEvent
    data class RemoteVolumeChange(val id: Int, val volume: Int) : VoiceCallScreenEvent
    data class LocalVolumeChange(val volume: Int) : VoiceCallScreenEvent
    data class ToggleMuteLocal(val value: Boolean) : VoiceCallScreenEvent
    data class ToggleMuteRemote(val id: Int, val value: Boolean) : VoiceCallScreenEvent
    data class ToggleCommunicationMode(val mode: CommunicationMode) : VoiceCallScreenEvent

    data class ToggleNoiseSuppressionMode(
        val enabled: Boolean,
        val mode: NoiseSuppressionMode
    ) : VoiceCallScreenEvent
}
