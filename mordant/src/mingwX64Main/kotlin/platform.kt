package com.github.ajalt.mordant

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.*


internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
    return memScoped {
        val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
        val success = GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), csbi.ptr)
        if (success == TRUE) {
            csbi.dwMaximumWindowSize.run { Y.toInt() to X.toInt() }
        } else {
            null
        }
    }
}
