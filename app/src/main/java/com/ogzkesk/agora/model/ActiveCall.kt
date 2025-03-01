package com.ogzkesk.agora.model

data class ActiveCall(
    val channelName: String,
    val channelId: Int,
    val remoteUsers: List<User>
)
