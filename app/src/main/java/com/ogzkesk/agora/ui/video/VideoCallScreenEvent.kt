package com.ogzkesk.agora.ui.video

import android.view.View
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode
import com.ogzkesk.agora.mvi.ViewEvent

sealed interface VideoCallScreenEvent : ViewEvent {
    data object EndCall : VideoCallScreenEvent
    data class RemoteVolumeChange(val id: Int, val volume: Int) : VideoCallScreenEvent
    data class LocalVolumeChange(val volume: Int) : VideoCallScreenEvent
    data class ToggleMuteLocal(val value: Boolean) : VideoCallScreenEvent
    data class ToggleMuteRemote(val id: Int, val value: Boolean) : VideoCallScreenEvent
    data class ToggleCommunicationMode(val mode: CommunicationMode) : VideoCallScreenEvent

    data class ToggleNoiseSuppressionMode(
        val enabled: Boolean,
        val mode: NoiseSuppressionMode
    ) : VideoCallScreenEvent

    data class LocalViewAttached(val view: View) : VideoCallScreenEvent
    data class RemoteViewAttached(val uid: Int, val view: View) : VideoCallScreenEvent
}