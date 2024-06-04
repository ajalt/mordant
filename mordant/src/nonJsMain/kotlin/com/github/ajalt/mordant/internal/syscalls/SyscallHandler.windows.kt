package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.KeyboardEvent
import kotlin.time.Duration
import kotlin.time.TimeSource

internal abstract class SyscallHandlerWindows : SyscallHandler {
    private companion object {
        // https://learn.microsoft.com/en-us/windows/console/key-event-record-str
        const val RIGHT_ALT_PRESSED: UInt = 0x0001u
        const val LEFT_ALT_PRESSED: UInt = 0x0002u
        const val RIGHT_CTRL_PRESSED: UInt = 0x0004u
        const val LEFT_CTRL_PRESSED: UInt = 0x0008u
        const val SHIFT_PRESSED: UInt = 0x0010u
        val CTRL_PRESSED_MASK = (RIGHT_CTRL_PRESSED or LEFT_CTRL_PRESSED)
        val ALT_PRESSED_MASK = (RIGHT_ALT_PRESSED or LEFT_ALT_PRESSED)
    }

    protected data class KeyEventRecord(
        val bKeyDown: Boolean,
        val wVirtualKeyCode: UShort,
        val uChar: Char,
        val dwControlKeyState: UInt,
    )

    protected abstract fun readRawKeyEvent(dwMilliseconds: Int): KeyEventRecord?

    override fun readKeyEvent(timeout: Duration): KeyboardEvent? {
        val t0 = TimeSource.Monotonic.markNow()
        while (t0.elapsedNow() < timeout) {
            val dwMilliseconds = (timeout - t0.elapsedNow()).inWholeMilliseconds
                .coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
            val event = readRawKeyEvent(dwMilliseconds)
            // ignore key up events
            if (event != null && event.bKeyDown) {
                val virtualName = WindowsVirtualKeyCodeToKeyEvent.getName(event.wVirtualKeyCode)
                return KeyboardEvent(
                    key = when {
                        virtualName != null -> virtualName
                        event.uChar.code != 0 -> event.uChar.toString()
                        else -> "Unidentified"
                    },
                    ctrl = event.dwControlKeyState and CTRL_PRESSED_MASK != 0u,
                    alt = event.dwControlKeyState and ALT_PRESSED_MASK != 0u,
                    shift = event.dwControlKeyState and SHIFT_PRESSED != 0u,
                )
            }
        }
        return null
    }
}
