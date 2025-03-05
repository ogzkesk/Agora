package com.ogzkesk.agora.lib

import android.util.Log
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.enums.EngineError
import com.ogzkesk.agora.lib.enums.NetworkQuality
import com.ogzkesk.agora.lib.model.ActiveCall
import com.ogzkesk.agora.lib.model.RemoteAudioState
import com.ogzkesk.agora.lib.model.User
import io.agora.rtc2.IRtcEngineEventHandler

class RtcEventListener(
    private val callCache: CallCache,
) : IRtcEngineEventHandler() {

    private val TAG = this::class.java.name

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        callCache.update {
            ActiveCall.create(channel.toString(), uid)
        }
        Log.i(TAG, "Joined channel: $channel uid: $uid elapsed: $elapsed")
    }

    override fun onError(err: Int) {
        super.onError(err)
        callCache.update {
            it?.copy(error = EngineError.fromErrorCode(err))
        }
        Log.e(TAG, "Error code: $err")
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        super.onLeaveChannel(stats)
        callCache.update { null }
        Log.i(
            TAG,
            "onLeaveChannel users: ${stats?.users} totalDuration: ${stats?.totalDuration}"
        )
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        callCache.update {
            it?.copy(remoteUsers = it.remoteUsers.plus(User.create(uid)))
        }
        Log.i(TAG, "User joined: $uid")
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        callCache.update { state ->
            val remoteUserList = state?.remoteUsers ?: return@update null
            val offlineUser = remoteUserList.find { it.id == uid } ?: return@update state
            val updatedUserList = remoteUserList.minus(offlineUser)
            state.copy(remoteUsers = updatedUserList)
        }
        Log.w(TAG, "User Offline: $uid")
    }

    override fun onRemoteAudioStateChanged(
        uid: Int,
        state: Int,
        reason: Int,
        elapsed: Int
    ) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        callCache.update {
            it?.copy(remoteAudioState = RemoteAudioState.create(uid, state, reason, elapsed))
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
        CommunicationMode.fromRoute(routing)
            ?.let { mode ->
                callCache.update { it?.copy(communicationMode = mode) }
                Log.i(TAG, "onAudioRouteChanged-> $mode")
            }
            ?: Log.i(TAG, "not implemented yet")
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        Log.i(TAG, "onConnectionStateChanged->, state->$state, reason->$reason")
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        super.onNetworkQuality(uid, txQuality, rxQuality)
        NetworkQuality.fromCode(txQuality)?.let {
            Log.d(
                TAG,
                "onNetworkQuality-> UID: $uid, TX Quality: ${it.name}, RX Quality: $rxQuality"
            )
        }
    }
}
