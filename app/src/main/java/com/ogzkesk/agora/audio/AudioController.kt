package com.ogzkesk.agora.audio

import android.util.Log
import com.ogzkesk.agora.model.EngineError
import com.ogzkesk.agora.model.RemoteAudioState
import com.ogzkesk.agora.model.User
import com.ogzkesk.agora.model.VoiceCall
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class AudioController(
    private val engine: RtcEngine
) {
    private val TAG = this::class.java.name
    private val mutableVoiceCall = MutableStateFlow<VoiceCall?>(null)
    val activeCallState = mutableVoiceCall.asStateFlow()

    init {
        engine.addHandler(
            object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    mutableVoiceCall.update { VoiceCall.create(channel.toString(), uid) }
                    Log.i(TAG, "Joined channel: $channel uid: $uid elapsed: $elapsed")
                }

                override fun onError(err: Int) {
                    super.onError(err)
                    mutableVoiceCall.update {
                        it?.copy(error = EngineError.fromErrorCode(err))
                    }
                    Log.e(TAG, "Error code: $err")
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    super.onLeaveChannel(stats)
                    mutableVoiceCall.update { null }
                    Log.i(
                        TAG,
                        "onLeaveChannel users: ${stats?.users} totalDuration: ${stats?.totalDuration}"
                    )
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    mutableVoiceCall.value = activeCallState.value?.copy(
                        remoteUsers = activeCallState.value?.remoteUsers?.plus(User.create(uid))
                            ?: emptyList()
                    )
                    Log.i(TAG, "User joined: $uid")
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    super.onUserOffline(uid, reason)
                    val user = activeCallState.value?.remoteUsers?.find { it.id == uid } ?: return
                    mutableVoiceCall.value = activeCallState.value?.copy(
                        remoteUsers = activeCallState.value?.remoteUsers?.minus(user) ?: emptyList()
                    )
                    Log.w(TAG, "User Offline: $uid")
                }

                override fun onRemoteAudioStateChanged(
                    uid: Int,
                    state: Int,
                    reason: Int,
                    elapsed: Int
                ) {
                    super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
                    mutableVoiceCall.update {
                        it?.copy(
                            remoteAudioState = RemoteAudioState.create(uid, state, reason, elapsed)
                        )
                    }
                    Log.i(TAG, "onRemoteAudioStateChanged->$uid, state->$state, reason->$reason")
                }

                override fun onTokenPrivilegeWillExpire(token: String?) {
                    super.onTokenPrivilegeWillExpire(token)
                    // if token expires request new token from server.
//                    engine.renewToken()
                }

                override fun onAudioRouteChanged(routing: Int) {
                    super.onAudioRouteChanged(routing)
                    CommunicationMode.entries.find { it.route == routing }
                        ?.let { mode ->
                            mutableVoiceCall.update { it?.copy(communicationMode = mode) }
                            Log.i(TAG, "onAudioRouteChanged-> $mode")
                        }
                        ?: Log.i(TAG, "not implemented yet")
                }
            }
        )
    }

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
        mutableVoiceCall.update { null }
    }

    fun toggleLocalAudio(value: Boolean) {
        engine.muteLocalAudioStream(value)
        mutableVoiceCall.update { it?.copy(isLocalMuted = value) }
    }

    fun toggleRemoteAudio(uid: Int, value: Boolean) {
        engine.muteRemoteAudioStream(uid, value)
        mutableVoiceCall.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(isMuted = value) else user
                }
            )
        }
    }

    fun setLocalVolume(volume: Int) {
        engine.adjustRecordingSignalVolume(volume)
        mutableVoiceCall.update { it?.copy(localVolume = volume) }
    }

    fun setRemoteVolume(uid: Int, volume: Int) {
        engine.adjustUserPlaybackSignalVolume(uid, volume)
        mutableVoiceCall.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(volume = volume) else user
                }
            )
        }
    }

    fun setCommunicationMode(mode: CommunicationMode) {
        engine.setRouteInCommunicationMode(mode.route)
        mutableVoiceCall.update { it?.copy(communicationMode = mode) }
    }

    fun setAINoiseSuppression(enabled: Boolean, mode: NoiseSuppressionMode) {
        val result = engine.setAINSMode(enabled, mode.code)
        if(result == 0){
            mutableVoiceCall.update {
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

    enum class CommunicationMode(val route: Int) {
        EARPIECE(Constants.AUDIO_ROUTE_EARPIECE),
        SPEAKER(Constants.AUDIO_ROUTE_SPEAKERPHONE);
    }

    enum class NoiseSuppressionMode(val code: Int) {
        BALANCE(0),
        AGGRESSIVE(1),
        AGGRESSIVE_LOW_LATENCY(2);
    }
}
