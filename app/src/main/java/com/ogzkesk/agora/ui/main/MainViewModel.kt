package com.ogzkesk.agora.ui.main

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.audio.AudioController
import com.ogzkesk.agora.mvi.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioController: AudioController,
) : ViewModel<MainScreenState, MainScreenEvent>(MainScreenState()) {

    init {
        viewModelScope.launch {
            audioController.activeCall.collect { call ->
                updateState {
                    it.copy(activeVoiceCall = call)
                }
            }
        }
    }

    override fun onUiEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ToggleTemporaryToken -> updateState { it.copy(useTemporaryToken = event.value) }
            is MainScreenEvent.ChannelNameChangedEvent -> updateState { it.copy(channelName = event.value) }
            is MainScreenEvent.StartVoiceCalling -> {
                if (state.value.useTemporaryToken) {
                    audioController.startVoiceCalling()
                } else {
                    audioController.startVoiceCalling(
                        channelName = state.value.channelName,
                        uid = 0
                    )
                }
            }
        }
    }
}
