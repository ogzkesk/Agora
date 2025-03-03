package com.ogzkesk.agora.ui.voice

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.audio.AudioController
import com.ogzkesk.agora.mvi.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceCallViewModel @Inject constructor(
    private val audioController: AudioController
) : ViewModel<VoiceCallScreenState, VoiceCallScreenEvent>(VoiceCallScreenState()) {

    init {
        viewModelScope.launch {
            audioController.activeCallState.collect { call ->
                updateState { it.copy(voiceCall = call) }
            }
        }
    }

    override fun onEvent(event: VoiceCallScreenEvent) {
        when (event) {
            VoiceCallScreenEvent.EndCall -> audioController.leaveVoiceCalling()
            is VoiceCallScreenEvent.LocalVolumeChange -> audioController.setLocalVolume(event.volume)
            is VoiceCallScreenEvent.ToggleMuteLocal -> audioController.toggleLocalAudio(event.value)
            is VoiceCallScreenEvent.ToggleCommunicationMode -> audioController.setCommunicationMode(event.mode)

            is VoiceCallScreenEvent.ToggleMuteRemote -> audioController.toggleRemoteAudio(
                event.id,
                event.value
            )

            is VoiceCallScreenEvent.RemoteVolumeChange -> audioController.setRemoteVolume(
                event.id,
                event.volume
            )
        }
    }
}