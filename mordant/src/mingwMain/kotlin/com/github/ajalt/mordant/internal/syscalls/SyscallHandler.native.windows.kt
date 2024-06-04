
package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.internal.Size
import kotlinx.cinterop.*
import platform.windows.*

internal object SyscallHandlerNativeWindows : SyscallHandlerWindows() {
    // https://docs.microsoft.com/en-us/windows/console/getconsolemode
    override fun stdoutInteractive(): Boolean = memScoped {
        GetConsoleMode(GetStdHandle(STD_OUTPUT_HANDLE), alloc<UIntVar>().ptr) != 0
    }

    override fun stdinInteractive(): Boolean = memScoped {
        GetConsoleMode(GetStdHandle(STD_INPUT_HANDLE), alloc<UIntVar>().ptr) != 0
    }

    override fun stderrInteractive(): Boolean = memScoped {
        GetConsoleMode(GetStdHandle(STD_ERROR_HANDLE), alloc<UIntVar>().ptr) != 0
    }

    // https://docs.microsoft.com/en-us/windows/console/getconsolescreenbufferinfo
    override fun getTerminalSize(): Size? = memScoped {
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

    override fun readRawKeyEvent(dwMilliseconds: Int): KeyEventRecord? = memScoped {
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
        return KeyEventRecord(
            bKeyDown = keyEvent.bKeyDown != 0,
            wVirtualKeyCode = keyEvent.wVirtualKeyCode,
            uChar = keyEvent.uChar.UnicodeChar.toInt().toChar(),
            dwControlKeyState = keyEvent.dwControlKeyState,
        )
    }

    override fun enterRawMode(): AutoCloseable = memScoped {
        val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
        val originalMode = alloc<UIntVar>()
        GetConsoleMode(stdinHandle, originalMode.ptr)

        // dwMode=0 means ctrl-c processing, echo, and line input modes are disabled. Could add
        // ENABLE_PROCESSED_INPUT, ENABLE_MOUSE_INPUT or ENABLE_WINDOW_INPUT if we want those
        // events.
        SetConsoleMode(stdinHandle, 0u)

        return AutoCloseable { SetConsoleMode(stdinHandle, originalMode.value) }
    }

}
