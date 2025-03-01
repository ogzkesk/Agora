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
                    it.copy(activeVoiceCall = call, isLoading = false)
                }
            }
        }
    }

    override fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.StartVoiceCalling -> {
                updateState {
                    it.copy(isLoading = true)
                }

                audioController.startVoiceCalling(
                    useTemporaryToken = state.value.useTemporaryToken,
                    channelName = state.value.channelName,
                    uid = 0
                ) { error ->
                    updateState {
                        it.copy(isLoading = false, errorMsg = error)
                    }
                }
            }

            is MainScreenEvent.ToggleTemporaryToken -> updateState {
                it.copy(useTemporaryToken = event.value)
            }

            is MainScreenEvent.ChannelNameChangedEvent -> updateState {
                it.copy(channelName = event.value)
            }

            MainScreenEvent.ResetErrorState -> updateState {
                it.copy(errorMsg = null)
            }
        }
    }
}
