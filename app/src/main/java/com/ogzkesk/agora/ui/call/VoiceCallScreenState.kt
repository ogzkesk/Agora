package com.ogzkesk.agora.ui.call

import com.ogzkesk.agora.model.ActiveCall

data class VoiceCallScreenState(
    val activeCall: ActiveCall? = null,
    val isLocalMuted: Boolean = false,
    val localVolume: Int = 100
)
