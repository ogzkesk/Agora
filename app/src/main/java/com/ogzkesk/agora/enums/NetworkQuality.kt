package com.ogzkesk.agora.enums

enum class NetworkQuality(val code: Int) {
    QUALITY_UNKNOWN(0),
    QUALITY_EXCELLENT(1),
    QUALITY_GOOD(2),
    QUALITY_POOR(3),
    QUALITY_BAD(4),
    QUALITY_VBAD(5),
    QUALITY_DOWN(6);

    companion object {
        fun fromCode(code: Int): NetworkQuality? {
            return entries.find { it.code == code }
        }
    }
}