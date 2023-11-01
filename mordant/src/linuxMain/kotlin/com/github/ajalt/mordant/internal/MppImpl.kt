package com.github.ajalt.mordant.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.STDIN_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.winsize

@OptIn(ExperimentalForeignApi::class)
internal actual fun getTerminalSize(): Pair<Int, Int>? {
    return memScoped {
        val size = alloc<winsize>()
        if (ioctl(STDIN_FILENO, TIOCGWINSZ.toULong(), size) < 0) {
            null
        } else {
            size.ws_col.toInt() to size.ws_row.toInt()
        }
    }
}
