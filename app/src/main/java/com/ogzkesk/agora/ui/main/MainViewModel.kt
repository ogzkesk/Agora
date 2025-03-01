package com.ogzkesk.agora.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import com.ogzkesk.agora.controller.AudioController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.IRtcEngineEventHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val audioController: AudioController = AudioController(context)
    private val mutableState = MutableStateFlow(MainState())
    val state = mutableState.asStateFlow()

    init {
        initializeController()
    }

    fun startVoiceCalling() {
        audioController.startVoiceCalling()
    }

    fun leaveVoiceCalling() {
        audioController.leaveVoiceCalling()
    }

    fun toggleMute(value: Boolean) {
        if (value) audioController.muteLocalAudio() else audioController.unMuteLocal()
    }

    fun toggleRemoteMute(uid: Int, value: Boolean) {
        if (value) audioController.muteRemoteAudio(uid) else audioController.unMuteRemoteAudio(uid)
    }

    private fun initializeController() {
        audioController.initialize(
            object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    println("Joined channel: $channel uid: $uid elapsed: $elapsed")
                }

                override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onRejoinChannelSuccess(channel, uid, elapsed)
                    println("Re-Joined channel: $channel uid: $uid elapsed: $elapsed")
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    super.onLeaveChannel(stats)
                    println("onLeaveChannel users: ${stats?.users} totalDuration: ${stats?.totalDuration}")
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    println("User joined: $uid")
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    super.onUserOffline(uid, reason)
                    println("User joined: $uid")
                }
            },
            object : AudioController.InteractionListener {
                override fun onLocalMuted(value: Boolean) {
                    mutableState.update { it.copy(isLocalMuted = value) }
                }

                override fun onRemoteMuted(uid: Int, value: Boolean) {
                    mutableState.update {
                        it.copy(
                            activeCall = it.activeCall?.copy(
                                remoteUsers = it.activeCall.remoteUsers.map { user ->
                                    if (user.id == uid) user.copy(isMuted = value) else user
                                }
                            )
                        )
                    }
                }
            }
        )
    }
}
