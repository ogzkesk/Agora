package com.ogzkesk.agora.ui.video

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.controller.Controller
import com.ogzkesk.agora.mvi.ViewModel
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
            is VideoCallScreenEvent.LocalViewAttached -> controller.attachLocalView(event.view)
            is VideoCallScreenEvent.RemoteViewAttached -> controller.attachRemoteView(
                event.uid,
                event.view
            )
            VideoCallScreenEvent.EndCall -> controller.leaveCall()
            is VideoCallScreenEvent.LocalVolumeChange -> {

            }
            is VideoCallScreenEvent.RemoteVolumeChange -> {

            }
            is VideoCallScreenEvent.ToggleCommunicationMode -> {

            }
            is VideoCallScreenEvent.ToggleMuteLocal -> {


            }
            is VideoCallScreenEvent.ToggleMuteRemote -> {

            }
            is VideoCallScreenEvent.ToggleNoiseSuppressionMode -> {

            }
        }
    }
}
