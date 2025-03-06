package com.ogzkesk.agora.lib.model

interface CallType {

    val tempToken: String?
    val channelName: String
    val uid: Int

    data class Voice(
        override val tempToken: String?,
        override val channelName: String,
        override val uid: Int,
    ) : CallType {
        companion object {
            val EMPTY = Voice(null, "", 0)
        }
    }

    data class Camera(
        override val tempToken: String?,
        override val channelName: String,
        override val uid: Int
    ) : CallType {
        companion object {
            val EMPTY = Camera(null, "", 0)
        }
    }
}
