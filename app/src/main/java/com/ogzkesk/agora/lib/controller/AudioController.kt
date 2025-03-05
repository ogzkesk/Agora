package com.ogzkesk.agora.lib.controller

import android.util.Log
import com.ogzkesk.agora.lib.CallCache
import com.ogzkesk.agora.lib.TokenUtils
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import kotlin.math.abs

class AudioController(
    private val engine: RtcEngine,
    private val callCache: CallCache,
) {
    private val TAG = this::class.java.name

    fun startVoiceCalling(
        useTemporaryToken: Boolean,
        // channelName should generated from backend.
        channelName: String,
        // userId "0" creates random uid for testing purpose, should generated from backend.
        uid: Int,
        onError: (String) -> Unit
    ) {
        val options = getDefaultChannelOptions()
        if (useTemporaryToken) {
            val res = engine.joinChannel(
                TokenUtils.TEMPORARY_TOKEN,
                TokenUtils.TEST_CHANNEL_NAME,
                uid,
                options
            )
            if (res != 0) {
                val errorMsg = RtcEngine.getErrorDescription(abs(res))
                onError(errorMsg)
                Log.i(TAG, "Error -> $errorMsg")
            }
        } else {
            // uses agora server to generate token.
            TokenUtils.generate(
                channelName,
                uid,
                { token ->
                    token?.let {
                        val res = engine.joinChannel(token, channelName, uid, options)
                        if (res != 0) {
                            val errorMsg = RtcEngine.getErrorDescription(abs(res))
                            onError(errorMsg)
                            Log.i(TAG, "Error -> $errorMsg")
                        }
                    }
                },
                { exc ->
                    onError(exc.message.toString())
                }
            )
        }
    }

    fun leaveVoiceCalling() {
        engine.leaveChannel()
        callCache.update { null }
    }

    fun toggleLocalAudio(value: Boolean) {
        engine.muteLocalAudioStream(value)
        callCache.update { it?.copy(isLocalMuted = value) }
    }

    fun toggleRemoteAudio(uid: Int, value: Boolean) {
        engine.muteRemoteAudioStream(uid, value)
        callCache.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(isMuted = value) else user
                }
            )
        }
    }

    fun setLocalVolume(volume: Int) {
        engine.adjustRecordingSignalVolume(volume)
        callCache.update { it?.copy(localVolume = volume) }
    }

    fun setRemoteVolume(uid: Int, volume: Int) {
        engine.adjustUserPlaybackSignalVolume(uid, volume)
        callCache.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(volume = volume) else user
                }
            )
        }
    }

    fun setCommunicationMode(mode: CommunicationMode) {
        engine.setRouteInCommunicationMode(mode.route)
        callCache.update { it?.copy(communicationMode = mode) }
    }

    fun setAINoiseSuppression(enabled: Boolean, mode: NoiseSuppressionMode) {
        val result = engine.setAINSMode(enabled, mode.code)
        if (result == 0) {
            callCache.update {
                it?.copy(noiseSuppressionMode = if (enabled) mode else null)
            }
        }
    }

    private fun getDefaultChannelOptions(): ChannelMediaOptions {
        return ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
        }
    }
}
