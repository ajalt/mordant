package com.github.ajalt.mordant.internal

/** Read bytes from a UTF-8 encoded stream, and return the next codepoint. */
internal fun readBytesAsUtf8(readByte: () -> Int?): Int? {
    val byte = readByte() ?: return null
    val byteLength: Int
    var codepoint: Int
    when {
        byte and 0b1000_0000 == 0x00 -> {
            return byte // 1-byte character
        }

        byte and 0b1110_0000 == 0b1100_0000 -> {
            codepoint = byte and 0b11111
            byteLength = 2
        }

        byte and 0b1111_0000 == 0b1110_0000 -> {
            codepoint = byte and 0b1111
            byteLength = 3
        }

        byte and 0b1111_1000 == 0b1111_0000 -> {
            codepoint = byte and 0b111
            byteLength = 4
        }

        else -> error("Invalid UTF-8 byte")
    }

    repeat(byteLength - 1) {
        val next = readByte() ?: return null
        if (next and 0b1100_0000 != 0b1000_0000) error("Invalid UTF-8 byte")
        codepoint = codepoint shl 6 or (next and 0b0011_1111)
    }
    return codepoint
}

/** Convert a unicode codepoint to a String. */
internal fun codepointToString(codePoint: Int): String {
    return when (codePoint) {
        in 0..0xFFFF -> {
            Char(codePoint).toString()
        }
        in 0x10000..0x10FFFF -> {
            val highSurrogate = Char(((codePoint - 0x10000) shr 10) or 0xD800)
            val lowSurrogate = Char(((codePoint - 0x10000) and 0x3FF) or 0xDC00)
            highSurrogate.toString() + lowSurrogate.toString()
        }
        else -> {
            throw IllegalArgumentException("Invalid code point: $codePoint")
        }
    }
}
