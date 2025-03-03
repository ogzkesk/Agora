package com.ogzkesk.agora.model

import com.ogzkesk.agora.audio.AudioController

data class VoiceCall(
    val channelName: String,
    val channelId: Int,
    val remoteUsers: List<User>,
    val communicationMode: AudioController.CommunicationMode,
    val isLocalMuted: Boolean,
    val localVolume: Int,
    val remoteAudioState: RemoteAudioState?,
) {
    companion object {
        fun create(channelName: String, uid: Int): VoiceCall {
            return VoiceCall(
                channelName = channelName,
                channelId = uid,
                remoteUsers = emptyList(),
                communicationMode = AudioController.CommunicationMode.SPEAKER,
                isLocalMuted = false,
                localVolume = 100,
                remoteAudioState = null
            )
        }
    }
}
