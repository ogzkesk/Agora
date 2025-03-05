package com.ogzkesk.agora.ui.main

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.controller.Controller
import com.ogzkesk.agora.lib.enums.EngineError
import com.ogzkesk.agora.mvi.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val controller: Controller,
    private val callCache: CallCache,
) : ViewModel<MainScreenState, MainScreenEvent>(MainScreenState()) {

    init {
        viewModelScope.launch {
            callCache.stream().collect { call ->
                updateState {
                    it.copy(
                        activeCall = call,
                        isLoading = false,
                        errorMsg = when (call?.error) {
                            EngineError.ERR_TOKEN_EXPIRED -> "Token is expired"
                            EngineError.ERR_INVALID_TOKEN -> "Invalid token"
                            else -> null
                        }
                    )
                }
            }
        }
    }

    override fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.StartVoiceCalling -> startCall(camera = false)
            is MainScreenEvent.StartVideoCalling -> startCall(camera = true)
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


    private fun startCall(camera: Boolean) {
        updateState {
            it.copy(isLoading = true)
        }

        controller.startCall(
            camera = camera,
            useTemporaryToken = state.value.useTemporaryToken,
            channelName = state.value.channelName,
            uid = 0
        ) { error ->
            updateState {
                it.copy(isLoading = false, errorMsg = error)
            }
        }
    }
}
