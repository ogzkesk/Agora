package com.ogzkesk.agora.ui.call

import com.ogzkesk.agora.model.VoiceCall

data class VoiceCallScreenState(
    val voiceCall: VoiceCall? = null,
    val isLocalMuted: Boolean = false,
    val localVolume: Int = 100
)
