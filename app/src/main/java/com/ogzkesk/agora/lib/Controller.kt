package com.ogzkesk.agora.lib

import android.util.Log
import android.view.SurfaceView
import android.view.View
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode
import com.ogzkesk.agora.lib.model.ActiveCall
import com.ogzkesk.agora.lib.model.CallType
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import kotlin.math.abs

class Controller(
    private val engine: RtcEngine,
    private val callCache: CallCache,
) {
    private val TAG = this::class.java.name

    fun startCall(
        callType: CallType,
        onError: (String) -> Unit
    ) {
        val isCamera = callType is CallType.Camera
        val options = if(isCamera){
            engine.enableVideo()
            engine.startPreview()
            getDefaultChannelOptions(camera = true)
        } else {
            getDefaultChannelOptions(camera = false)
        }

        callType.tempToken?.let { token ->
            val res = engine.joinChannel(
                token,
                callType.channelName,
                callType.uid,
                options
            )
            if (res != 0) {
                val errorMsg = RtcEngine.getErrorDescription(abs(res))
                onError(errorMsg)
                Log.i(TAG, "Error -> $errorMsg")
            } else {
                callCache.update {
                    ActiveCall.create(callType.channelName, callType.uid, callType)
                }
            }
        } ?: run {
            // uses agora server to generate token.
            TokenUtils.generate(
                callType.channelName,
                callType.uid,
                { token ->
                    token?.let {
                        val res =
                            engine.joinChannel(token, callType.channelName, callType.uid, options)
                        if (res != 0) {
                            val errorMsg = RtcEngine.getErrorDescription(abs(res))
                            onError(errorMsg)
                            Log.i(TAG, "Error -> $errorMsg")
                        } else {
                            callCache.update {
                                ActiveCall.create(callType.channelName, callType.uid, callType)
                            }
                        }
                    }
                },
                { exc ->
                    onError(exc.message.toString())
                    callCache.update { null }
                }
            )
        }
    }

    fun leaveCall() {
        engine.leaveChannel()
        engine.stopPreview()
        engine.disableVideo()
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

    fun attachLocalView(view: View) {
        engine.setupLocalVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_ADAPTIVE, 0))
    }

    fun attachRemoteView(uid: Int, view: View) {
        engine.setupRemoteVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_ADAPTIVE, uid))
    }

    private fun getDefaultChannelOptions(camera: Boolean): ChannelMediaOptions {
        return ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = camera
        }
    }
}
