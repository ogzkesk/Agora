package com.ogzkesk.agora.ui.main

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.controller.AudioController
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
                updateState { it.copy(activeCall = call) }
            }
        }
    }

    override fun onUiEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.StartVoiceCalling -> audioController.startVoiceCalling()
        }
    }
}
