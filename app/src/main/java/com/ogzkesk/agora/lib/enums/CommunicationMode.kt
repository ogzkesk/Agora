package com.ogzkesk.agora.lib.enums

enum class CommunicationMode(val route: Int) {
    EARPIECE(1),
    SPEAKER(3);

    companion object {
        fun fromRoute(route: Int): CommunicationMode? {
            return entries.find { it.route == route }
        }
    }
}