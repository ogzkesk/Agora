package com.ogzkesk.agora.ui.video

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.Controller
import com.ogzkesk.agora.mvi.ViewModel
import com.ogzkesk.agora.ui.voice.VoiceCallScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val controller: Controller,
    private val callCache: CallCache,
) : ViewModel<VideoCallScreenState, VideoCallScreenEvent>(VideoCallScreenState()) {

    init {
        viewModelScope.launch {
            callCache.stream().collect { call ->
                updateState {
                    it.copy(activeCall = call)
                }
            }
        }
    }

    override fun onEvent(event: VideoCallScreenEvent) {
        when (event) {
            VideoCallScreenEvent.EndCall -> controller.leaveCall()
            is VideoCallScreenEvent.LocalViewAttached -> controller.attachLocalView(event.view)
            is VideoCallScreenEvent.RemoteViewAttached -> controller.attachRemoteView(
                event.uid,
                event.view
            )
            is VideoCallScreenEvent.LocalVolumeChange -> controller.setLocalVolume(event.volume)
            is VideoCallScreenEvent.ToggleMuteLocal -> controller.toggleLocalAudio(event.value)
            is VideoCallScreenEvent.ToggleCommunicationMode -> controller.setCommunicationMode(
                event.mode
            )

            is VideoCallScreenEvent.ToggleMuteRemote -> controller.toggleRemoteAudio(
                event.id,
                event.value
            )

            is VideoCallScreenEvent.RemoteVolumeChange -> controller.setRemoteVolume(
                event.id,
                event.volume
            )

            is VideoCallScreenEvent.ToggleNoiseSuppressionMode -> controller.setAINoiseSuppression(
                event.enabled,
                event.mode
            )
        }
    }
}
