package com.ogzkesk.agora.lib.model

import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.EngineError
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode

data class ActiveCall(
    val channelName: String,
    val channelId: Int,
    val remoteUsers: List<User>,
    val communicationMode: CommunicationMode,
    val noiseSuppressionMode: NoiseSuppressionMode?,
    val isLocalMuted: Boolean,
    val localVolume: Int,
    val remoteAudioState: RemoteAudioState?,
    val error: EngineError?,
) {
    companion object {
        fun create(channelName: String, uid: Int): ActiveCall {
            return ActiveCall(
                channelName = channelName,
                channelId = uid,
                remoteUsers = emptyList(),
                communicationMode = CommunicationMode.SPEAKER,
                noiseSuppressionMode = null,
                isLocalMuted = false,
                localVolume = 100,
                remoteAudioState = null,
                error = null
            )
        }
    }
}
