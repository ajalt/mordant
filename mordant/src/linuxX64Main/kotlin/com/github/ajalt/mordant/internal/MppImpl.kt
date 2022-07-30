package com.github.ajalt.mordant.internal

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.*

@OptIn(ExperimentalUnsignedTypes::class)
internal actual fun getTerminalSize(timeoutMs: Long, throwExceptions: Boolean): Pair<Int, Int>? {
    return memScoped {
        val size = alloc<winsize>()
        if (ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
            null
        } else {
            size.ws_col.toInt() to size.ws_row.toInt()
        }
    }
}

internal actual fun isWindows(): Boolean = false
