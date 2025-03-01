package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.model.ActiveCall

data class MainState(
    val activeCall: ActiveCall? = null,
    val isLocalMuted: Boolean = false
)
