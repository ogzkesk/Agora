package com.ogzkesk.agora.controller

import android.content.Context
import com.ogzkesk.agora.model.ActiveCall
import com.ogzkesk.agora.model.User
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudioController(
    private val context: Context
) {
    private val myAppId = "dd455c171eed4a1f8be5722b76b58b94"
    private val channelName = "test-channel"
    private val token =
        "007eJxTYFBYLmKaF3Foqq+ixsxZxw9yGHF88eeJtz4vkJl612U/p4MCQ0qKialpsqG5YWpqikmiYZpFUqqpuZFRkrlZkqlFkqWJRfSh9IZARgYmHnNGRgYIBPF5GEpSi0t0kzMS8/JScxgYACDCHjQ="

    private var mRtcEngine: RtcEngine? = null
    private val mutableActiveCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall = mutableActiveCall.asStateFlow()

    fun initialize() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = this@AudioController.context
                mAppId = myAppId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        super.onJoinChannelSuccess(channel, uid, elapsed)
                        mutableActiveCall.update {
                            ActiveCall(channelName, uid, emptyList())
                        }
                        println("Joined channel: $channel uid: $uid elapsed: $elapsed")
                    }

                    override fun onLeaveChannel(stats: RtcStats?) {
                        super.onLeaveChannel(stats)
                        mutableActiveCall.update {
                            null
                        }
                        println("onLeaveChannel users: ${stats?.users} totalDuration: ${stats?.totalDuration}")
                    }

                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        mutableActiveCall.value = activeCall.value?.copy(
                            remoteUsers = activeCall.value?.remoteUsers?.plus(User.create(uid))
                                ?: emptyList()
                        )
                        println("User joined: $uid")
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        super.onUserOffline(uid, reason)
                        val user = activeCall.value?.remoteUsers?.find { it.id == uid } ?: return
                        mutableActiveCall.value = activeCall.value?.copy(
                            remoteUsers = activeCall.value?.remoteUsers?.minus(user) ?: emptyList()
                        )
                        println("User Offline: $uid")
                    }
                }
            }
            this.mRtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }
    }

    fun startVoiceCalling(
        channelName: String = this.channelName,
        uid: Int = 0 // userId "0" creates random uid
    ) {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
        }
        mRtcEngine?.joinChannel(token, channelName, uid, options)
    }

    fun leaveVoiceCalling() {
        mRtcEngine?.leaveChannel()
    }

    fun toggleLocalAudio(value: Boolean) {
        mRtcEngine?.muteLocalAudioStream(value)
    }

    fun toggleRemoteAudio(uid: Int, value: Boolean) {
        mRtcEngine?.muteRemoteAudioStream(uid, value)
        mutableActiveCall.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(isMuted = value) else user
                }
            )
        }
    }

    fun setLocalVolume(volume: Int) {
        mRtcEngine?.adjustRecordingSignalVolume(volume)
    }

    fun setRemoteVolume(uid: Int, volume: Int) {
        mRtcEngine?.adjustUserPlaybackSignalVolume(uid, volume)
        mutableActiveCall.update {
            it?.copy(
                remoteUsers = it.remoteUsers.map { user ->
                    if (user.id == uid) user.copy(volume = volume) else user
                }
            )
        }
    }
}
