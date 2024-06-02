package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandler
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativeWindows
import kotlinx.cinterop.*
import platform.windows.*

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

internal actual fun hasFileSystem(): Boolean = true
internal actual fun getSyscallHandler(): SyscallHandler = SyscallHandlerNativeWindows
