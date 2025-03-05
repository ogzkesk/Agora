package com.ogzkesk.agora.ui.main

import com.ogzkesk.agora.lib.model.ActiveCall

data class MainScreenState(
    val activeCall: ActiveCall? = null,
    val useTemporaryToken: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val channelName: String = ""
)
