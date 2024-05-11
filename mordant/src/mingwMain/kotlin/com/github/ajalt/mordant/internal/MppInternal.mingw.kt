@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.mordant.internal

import kotlinx.cinterop.*
import platform.windows.*


// https://docs.microsoft.com/en-us/windows/console/getconsolescreenbufferinfo
internal actual fun getTerminalSize(): Size? = memScoped {
    val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
    val stdoutHandle = GetStdHandle(STD_OUTPUT_HANDLE)
    if (stdoutHandle == INVALID_HANDLE_VALUE) {
        return@memScoped null
    }

    if (GetConsoleScreenBufferInfo(stdoutHandle, csbi.ptr) == 0) {
        return@memScoped null
    }
    csbi.srWindow.run { Size(width = Right - Left + 1, height = Bottom - Top + 1) }
}

// https://docs.microsoft.com/en-us/windows/console/setconsolemode
// https://docs.microsoft.com/en-us/windows/console/getconsolemode
internal actual fun ttySetEcho(echo: Boolean) = memScoped {
    val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
    if (stdinHandle == INVALID_HANDLE_VALUE) {
        return@memScoped
    }
    val lpMode = alloc<UIntVar>()
    if (GetConsoleMode(stdinHandle, lpMode.ptr) == 0) {
        return@memScoped
    }

    val newMode = if (echo) {
        lpMode.value or ENABLE_ECHO_INPUT.convert()
    } else {
        lpMode.value and ENABLE_ECHO_INPUT.inv().convert()
    }
    SetConsoleMode(stdinHandle, newMode)
}

internal actual fun cwd(): String = memScoped {
    val buf = allocArray<ByteVar>(4096)
    GetCurrentDirectoryA(4096U, buf)
    return buf.toKString()
}
