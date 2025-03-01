package com.ogzkesk.agora.controller

import android.content.Context
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class AudioController(
    private val context: Context
) {
    private val myAppId = "dd455c171eed4a1f8be5722b76b58b94"
    private val channelName = "test-channel"
    private val token =
        "007eJxTYFBYLmKaF3Foqq+ixsxZxw9yGHF88eeJtz4vkJl612U/p4MCQ0qKialpsqG5YWpqikmiYZpFUqqpuZFRkrlZkqlFkqWJRfSh9IZARgYmHnNGRgYIBPF5GEpSi0t0kzMS8/JScxgYACDCHjQ="
    private var mRtcEngine: RtcEngine? = null
    private var interactionListener: InteractionListener? = null

    fun initialize(
        eventHandler: IRtcEngineEventHandler,
        interactionListener: InteractionListener
    ) {
        try {
            val config = RtcEngineConfig().apply {
                mContext = this@AudioController.context
                mAppId = myAppId
                mEventHandler = eventHandler
            }
            this.mRtcEngine = RtcEngine.create(config)
            this.interactionListener = interactionListener
        } catch (e: Exception) {
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }
    }

    fun startVoiceCalling() {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
        }
        mRtcEngine?.joinChannel(token, channelName, 0, options)
    }

    fun leaveVoiceCalling() {
        mRtcEngine?.leaveChannel()
    }

    fun muteLocalAudio() {
        mRtcEngine?.muteLocalAudioStream(true)
        interactionListener?.onLocalMuted(true)
    }

    fun unMuteLocal() {
        mRtcEngine?.muteLocalAudioStream(false)
        interactionListener?.onLocalMuted(false)
    }

    fun muteRemoteAudio(uid: Int) {
        mRtcEngine?.muteRemoteAudioStream(uid, true)
        interactionListener?.onRemoteMuted(uid, true)
    }

    fun unMuteRemoteAudio(uid: Int) {
        mRtcEngine?.muteRemoteAudioStream(uid, false)
        interactionListener?.onRemoteMuted(uid, false)
    }

    interface InteractionListener {
        fun onLocalMuted(value: Boolean)
        fun onRemoteMuted(uid: Int, value: Boolean)
    }
}
