package com.ogzkesk.agora.ui.voice

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.controller.Controller
import com.ogzkesk.agora.mvi.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceCallViewModel @Inject constructor(
    private val controller: Controller,
    private val callCache: CallCache,
) : ViewModel<VoiceCallScreenState, VoiceCallScreenEvent>(VoiceCallScreenState()) {

    init {
        viewModelScope.launch {
            callCache.stream().collect { call ->
                updateState { it.copy(activeCall = call) }
            }
        }
    }

    override fun onEvent(event: VoiceCallScreenEvent) {
        when (event) {
            VoiceCallScreenEvent.EndCall -> controller.leaveCall()
            is VoiceCallScreenEvent.LocalVolumeChange -> controller.setLocalVolume(event.volume)
            is VoiceCallScreenEvent.ToggleMuteLocal -> controller.toggleLocalAudio(event.value)
            is VoiceCallScreenEvent.ToggleCommunicationMode -> controller.setCommunicationMode(
                event.mode
            )

            is VoiceCallScreenEvent.ToggleMuteRemote -> controller.toggleRemoteAudio(
                event.id,
                event.value
            )

            is VoiceCallScreenEvent.RemoteVolumeChange -> controller.setRemoteVolume(
                event.id,
                event.volume
            )

            is VoiceCallScreenEvent.ToggleNoiseSuppressionMode -> controller.setAINoiseSuppression(
                event.enabled,
                event.mode
            )
        }
    }
}