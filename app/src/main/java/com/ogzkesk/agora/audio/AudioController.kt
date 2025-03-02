package com.ogzkesk.agora.audio

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
    private val mutableVoiceCall = MutableStateFlow<VoiceCall?>(null)
    val activeCall = mutableVoiceCall.asStateFlow()

    init {
        engine.addHandler(
            object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    mutableVoiceCall.update {
                        VoiceCall(channel.toString(), uid, emptyList())
                    }
                    println("Joined channel: $channel uid: $uid elapsed: $elapsed")
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    super.onLeaveChannel(stats)
                    mutableVoiceCall.update {
                        null
                    }
                    println("onLeaveChannel users: ${stats?.users} totalDuration: ${stats?.totalDuration}")
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    mutableVoiceCall.value = activeCall.value?.copy(
                        remoteUsers = activeCall.value?.remoteUsers?.plus(User.create(uid))
                            ?: emptyList()
                    )
                    println("User joined: $uid")
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    super.onUserOffline(uid, reason)
                    val user = activeCall.value?.remoteUsers?.find { it.id == uid } ?: return
                    mutableVoiceCall.value = activeCall.value?.copy(
                        remoteUsers = activeCall.value?.remoteUsers?.minus(user) ?: emptyList()
                    )
                    println("User Offline: $uid")
                }

                override fun onRemoteAudioStateChanged(
                    uid: Int,
                    state: Int,
                    reason: Int,
                    elapsed: Int
                ) {
                    super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
                    println("onRemoteAudioStateChanged->$uid, state->$state, reason->$reason")
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
                println("Error -> $errorMsg")
            }
        } else {
            TokenUtils.generate(
                channelName,
                uid,
                { token ->
                    token?.let {
                        val res = engine.joinChannel(token, channelName, uid, options)
                        if (res != 0) {
                            val errorMsg = RtcEngine.getErrorDescription(abs(res))
                            onError(errorMsg)
                            println("Error -> $errorMsg")
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
    }

    fun toggleLocalAudio(value: Boolean) {
        engine.muteLocalAudioStream(value)
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

    private fun getDefaultChannelOptions(): ChannelMediaOptions {
        return ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
        }
    }
}
