package com.ogzkesk.agora.model

data class VoiceCall(
    val channelName: String,
    val channelId: Int,
    val remoteUsers: List<User>
)
