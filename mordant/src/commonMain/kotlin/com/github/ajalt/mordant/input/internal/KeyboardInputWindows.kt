package com.github.ajalt.mordant.input.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import kotlin.time.Duration
import kotlin.time.TimeSource

internal object KeyboardInputWindows {
    // https://learn.microsoft.com/en-us/windows/console/key-event-record-str
    private const val RIGHT_ALT_PRESSED: UInt = 0x0001u
    private const val LEFT_ALT_PRESSED: UInt = 0x0002u
    private const val RIGHT_CTRL_PRESSED: UInt = 0x0004u
    private const val LEFT_CTRL_PRESSED: UInt = 0x0008u
    private const val SHIFT_PRESSED: UInt = 0x0010u
    private val CTRL_PRESSED_MASK = (RIGHT_CTRL_PRESSED or LEFT_CTRL_PRESSED)
    private val ALT_PRESSED_MASK = (RIGHT_ALT_PRESSED or LEFT_ALT_PRESSED)

    data class KeyEventRecord(
        val bKeyDown: Boolean,
        val wVirtualKeyCode: UShort,
        val uChar: Char,
        val dwControlKeyState: UInt,
    )

    fun readKeyEvent(
        timeout: Duration,
        readRawKeyEvent: (dwMilliseconds: Int) -> KeyEventRecord?,
    ): KeyboardEvent? {
        val t0 = TimeSource.Monotonic.markNow()
        while (t0.elapsedNow() < timeout) {
            val dwMilliseconds = (timeout - t0.elapsedNow()).inWholeMilliseconds
                .coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
            val event = readRawKeyEvent(dwMilliseconds)
            // ignore key up events
            if (event != null && event.bKeyDown) {
                return KeyboardEvent(
                    key = when {
                        event.uChar.code != 0 -> event.uChar.toString()
                        else -> WindowsVirtualKeyCodeToKeyEvent.getName(event.wVirtualKeyCode)
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
