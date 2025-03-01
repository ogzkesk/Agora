package com.ogzkesk.agora.ui.call

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.controller.AudioController
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
            audioController.activeCall.collect { call ->
                updateState { it.copy(activeCall = call) }
            }
        }
    }

    override fun onUiEvent(event: VoiceCallScreenEvent) {
        when (event) {
            VoiceCallScreenEvent.EndCall -> {
                audioController.leaveVoiceCalling()
                updateState {
                    it.copy(activeCall = null)
                }
            }

            is VoiceCallScreenEvent.RemoteVolumeChange -> {
                audioController.setRemoteVolume(event.id, event.volume)
            }

            is VoiceCallScreenEvent.LocalVolumeChange -> {
                audioController.setLocalVolume(event.volume)
                updateState {
                    it.copy(localVolume = event.volume)
                }
            }

            is VoiceCallScreenEvent.ToggleMuteLocal -> {
                audioController.toggleLocalAudio(event.value)
            }

            is VoiceCallScreenEvent.ToggleMuteRemote -> {
                audioController.toggleRemoteAudio(event.id, event.value)
            }
        }
    }
}