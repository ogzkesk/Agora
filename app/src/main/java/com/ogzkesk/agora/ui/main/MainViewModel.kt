package com.ogzkesk.agora.ui.main

import androidx.lifecycle.viewModelScope
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.Controller
import com.ogzkesk.agora.lib.enums.EngineError
import com.ogzkesk.agora.lib.model.CallType
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
            is MainScreenEvent.ToggleTemporaryToken -> updateState {
                it.copy(useTemporaryToken = event.value)
            }

            is MainScreenEvent.ChannelNameChangedEvent -> updateState {
                it.copy(channelName = event.value)
            }

            is MainScreenEvent.TempTokenChangedEvent -> updateState {
                it.copy(tempToken = event.value)
            }

            MainScreenEvent.ResetErrorState -> updateState {
                it.copy(errorMsg = null)
            }

            is MainScreenEvent.StartVoiceCalling -> withState {
                if(!checkFields()) return@withState
                startCall(
                    callType = CallType.Voice(
                        tempToken = if (useTemporaryToken) tempToken else null,
                        channelName = channelName,
                        uid = 0
                    )
                )
            }

            is MainScreenEvent.StartVideoCalling -> withState {
                if(!checkFields()) return@withState
                startCall(
                    callType = CallType.Camera(
                        tempToken = if (useTemporaryToken) tempToken else null,
                        channelName = channelName,
                        uid = 0
                    )
                )
            }
        }
    }


    private fun startCall(callType: CallType) {
        updateState {
            it.copy(isLoading = true)
        }
        controller.startCall(
            callType = callType
        ) { error ->
            updateState {
                it.copy(isLoading = false, errorMsg = error)
            }
        }
    }

    private fun checkFields(): Boolean {
        if (state.value.channelName.isEmpty() ||
            state.value.useTemporaryToken && state.value.tempToken.isEmpty()
        ) {
            updateState {
                it.copy(errorMsg = "Enter required fields")
            }
            return false
        }
        return true
    }
}
