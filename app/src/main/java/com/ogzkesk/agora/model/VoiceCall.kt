package com.ogzkesk.agora.model

import com.ogzkesk.agora.audio.AudioController
import com.ogzkesk.agora.enums.CommunicationMode
import com.ogzkesk.agora.enums.EngineError
import com.ogzkesk.agora.enums.NoiseSuppressionMode

data class VoiceCall(
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
        fun create(channelName: String, uid: Int): VoiceCall {
            return VoiceCall(
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
