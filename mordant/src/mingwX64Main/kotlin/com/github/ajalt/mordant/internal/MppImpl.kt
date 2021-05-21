package com.github.ajalt.mordant.internal

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.*


internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
    return memScoped {
        val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
        val success = GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), csbi.ptr)
        if (success == TRUE) {
            csbi.dwSize.run { X.toInt() to Y.toInt() }
        } else {
            null
        }
    }
}

internal actual fun isWindows(): Boolean = true
