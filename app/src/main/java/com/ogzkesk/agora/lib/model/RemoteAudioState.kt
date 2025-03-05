package com.ogzkesk.agora.lib.model

import io.agora.rtc2.Constants

data class RemoteAudioState(
    val uid: Int,
    val state: State,
    val reason: Reason,
    val elapsed: Int,
) {
    enum class State {
        STATE_STOPPED,
        STATE_STARTING,
        STATE_FAILED,
        STATE_FROZEN,
        STATE_DECODING;
    }

    enum class Reason {
        INTERNAL,
        LOCAL_MUTED,
        LOCAL_UNMUTED,
        LOCAL_PLAY_FAILED,
        REMOTE_MUTED,
        REMOTE_UNMUTED,
        REMOTE_OFFLINE,
        NETWORK_CONGESTION,
        NETWORK_RECOVERY,
        NO_PACKET_RECEIVE;
    }

    companion object {
        fun create(uid: Int, state: Int, reason: Int, elapsed: Int) = RemoteAudioState(
            uid = uid,
            state = mapState(state),
            reason = mapReason(reason),
            elapsed = elapsed
        )

        private fun mapState(state: Int) = when (state) {
            Constants.REMOTE_AUDIO_STATE_STOPPED -> State.STATE_STOPPED
            Constants.REMOTE_AUDIO_STATE_STARTING -> State.STATE_STARTING
            Constants.REMOTE_AUDIO_STATE_FAILED -> State.STATE_FAILED
            Constants.REMOTE_AUDIO_STATE_FROZEN -> State.STATE_FROZEN
            Constants.REMOTE_AUDIO_STATE_DECODING -> State.STATE_DECODING
            else -> State.STATE_STOPPED
        }

        private fun mapReason(reason: Int) = when (reason) {
            Constants.REMOTE_AUDIO_REASON_INTERNAL -> Reason.INTERNAL
            Constants.REMOTE_AUDIO_REASON_LOCAL_MUTED -> Reason.LOCAL_MUTED
            Constants.REMOTE_AUDIO_REASON_LOCAL_UNMUTED -> Reason.LOCAL_UNMUTED
            Constants.REMOTE_AUDIO_REASON_LOCAL_PLAY_FAILED -> Reason.LOCAL_PLAY_FAILED
            Constants.REMOTE_AUDIO_REASON_REMOTE_MUTED -> Reason.REMOTE_MUTED
            Constants.REMOTE_AUDIO_REASON_REMOTE_UNMUTED -> Reason.REMOTE_UNMUTED
            Constants.REMOTE_AUDIO_REASON_REMOTE_OFFLINE -> Reason.REMOTE_OFFLINE
            Constants.REMOTE_AUDIO_REASON_NETWORK_CONGESTION -> Reason.NETWORK_CONGESTION
            Constants.REMOTE_AUDIO_REASON_NETWORK_RECOVERY -> Reason.NETWORK_RECOVERY
            Constants.REMOTE_AUDIO_REASON_NO_PACKET_RECEIVE -> Reason.NO_PACKET_RECEIVE
            else -> Reason.INTERNAL
        }
    }
}
