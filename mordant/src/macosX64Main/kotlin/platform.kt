package com.github.ajalt.mordant

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.STDIN_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.winsize

internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
    return memScoped {
        val size = alloc<winsize>()
        if (ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
            null
        } else {
            size.ws_col.toInt() to size.ws_row.toInt()
        }
    }
}
