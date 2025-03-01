package com.ogzkesk.agora.model

data class User(
    val id: Int,
    val isMuted: Boolean,
    val volume: Int
) {
    companion object {
        fun create(uid: Int): User {
            return User(
                id = uid,
                isMuted = false,
                volume = 100
            )
        }
    }
}
