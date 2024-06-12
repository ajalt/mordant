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

    override fun readRawEvent(dwMilliseconds: Int): EventRecord? = memScoped {
        val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
        val waitResult = WaitForSingleObject(stdinHandle, dwMilliseconds.toUInt())
        if (waitResult != 0u) return null
        val inputEvents = allocArray<INPUT_RECORD>(1)
        val eventsRead = alloc<UIntVar>()
        ReadConsoleInput!!(stdinHandle, inputEvents, 1u, eventsRead.ptr)
        if (eventsRead.value == 0u) {
            return null
        }
        val inputEvent = inputEvents[0]
        return when (inputEvent.EventType.toInt()) {
            KEY_EVENT -> {
                val keyEvent = inputEvent.Event.KeyEvent
                EventRecord.Key(
                    bKeyDown = keyEvent.bKeyDown != 0,
                    wVirtualKeyCode = keyEvent.wVirtualKeyCode,
                    uChar = keyEvent.uChar.UnicodeChar.toInt().toChar(),
                    dwControlKeyState = keyEvent.dwControlKeyState,
                )
            }

            MOUSE_EVENT -> {
                val mouseEvent = inputEvent.Event.MouseEvent
                EventRecord.Mouse(
                    dwMousePositionX = mouseEvent.dwMousePosition.X,
                    dwMousePositionY = mouseEvent.dwMousePosition.Y,
                    dwButtonState = mouseEvent.dwButtonState,
                    dwControlKeyState = mouseEvent.dwControlKeyState,
                    dwEventFlags = mouseEvent.dwEventFlags,
                )
            }

            else -> null
        }
    }

    override fun getStdinConsoleMode(): UInt? {
        val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
        return getConsoleMode(stdinHandle)
    }

    override fun setStdinConsoleMode(dwMode: UInt): Boolean {
        val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
        return SetConsoleMode(stdinHandle, 0u) != 0
    }

    fun ttySetEcho(echo: Boolean) = memScoped {
        val stdinHandle = GetStdHandle(STD_INPUT_HANDLE)
        val lpMode = getConsoleMode(stdinHandle) ?: return@memScoped
        val newMode = if (echo) {
            lpMode or ENABLE_ECHO_INPUT.convert()
        } else {
            lpMode and ENABLE_ECHO_INPUT.inv().convert()
        }
        SetConsoleMode(stdinHandle, newMode)
    }

    // https://docs.microsoft.com/en-us/windows/console/getconsolemode
    private fun getConsoleMode(handle: HANDLE?): UInt? = memScoped {
        if (handle == null || handle == INVALID_HANDLE_VALUE) return null
        val lpMode = alloc<UIntVar>()
        // "If the function succeeds, the return value is nonzero."
        if (GetConsoleMode(handle, lpMode.ptr) == 0) return null
        return lpMode.value
    }
}
