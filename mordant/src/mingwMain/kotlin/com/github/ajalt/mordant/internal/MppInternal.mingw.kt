@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.internal.KeyboardInputWindows
import kotlinx.cinterop.*
import platform.windows.*
import kotlin.time.Duration


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

internal actual fun hasFileSystem(): Boolean = true

internal actual fun readKeyMpp(timeout: Duration): KeyboardEvent? {
    return KeyboardInputWindows.readKeyEvent(timeout, ::readRawKeyEvent)
}

private fun readRawKeyEvent(dwMilliseconds: Int): KeyboardInputWindows.KeyEventRecord? = memScoped {
    val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
    val waitResult = WaitForSingleObject(stdinHandle, dwMilliseconds.toUInt())
    if (waitResult != 0u) return null
    val inputEvents = allocArray<INPUT_RECORD>(1)
    val eventsRead = alloc<UIntVar>()
    ReadConsoleInput!!(stdinHandle, inputEvents, 1u, eventsRead.ptr)
    if (eventsRead.value == 0u) {
        return null
    }
    val keyEvent = inputEvents[0].Event.KeyEvent
    return KeyboardInputWindows.KeyEventRecord(
        bKeyDown = keyEvent.bKeyDown != 0,
        wVirtualKeyCode = keyEvent.wVirtualKeyCode,
        uChar = keyEvent.uChar.UnicodeChar.toInt().toChar(),
        dwControlKeyState = keyEvent.dwControlKeyState,
    )
}

internal actual fun enterRawModeMpp(): AutoCloseable = memScoped{
    val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
    val originalMode = alloc<UIntVar>()
    GetConsoleMode(stdinHandle, originalMode.ptr)

    // only ENABLE_PROCESSED_INPUT means echo and line input modes are disabled. Could add
    // ENABLE_MOUSE_INPUT or ENABLE_WINDOW_INPUT if we want those events.
    // TODO: handle errors remove ENABLE_PROCESSED_INPUT to intercept ctrl-c
    SetConsoleMode(stdinHandle, ENABLE_PROCESSED_INPUT.toUInt())

    return AutoCloseable { SetConsoleMode(stdinHandle, originalMode.value) }
}
