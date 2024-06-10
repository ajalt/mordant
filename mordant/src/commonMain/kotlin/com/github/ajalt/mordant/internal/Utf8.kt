package com.github.ajalt.mordant.internal

/** Read bytes from a UTF-8 encoded stream, and return the next codepoint. */
fun readBytesAsUtf8(readByte: () -> Int): Int {
    val byte = readByte()
    var byteLength = 0
    var codepoint = 0
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
        val next = readByte()
        if (next and 0b1100_0000 != 0b1000_0000) error("Invalid UTF-8 byte")
        codepoint = codepoint shl 6 or (next and 0b0011_1111)
    }
    return codepoint
}
