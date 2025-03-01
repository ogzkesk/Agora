package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.model.VoiceCall

data class MainScreenState(
    val activeVoiceCall: VoiceCall? = null,
    val useTemporaryToken: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val channelName: String = ""
)
