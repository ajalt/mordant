package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.MouseEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.terminal.StandardTerminalInterface
import kotlin.time.TimeMark

/**
 * A base TerminalInterface implementation for Windows systems.
 */
abstract class TerminalInterfaceWindows : StandardTerminalInterface() {
    private companion object {
        // https://learn.microsoft.com/en-us/windows/console/key-event-record-str
        const val RIGHT_ALT_PRESSED: UInt = 0x0001u
        const val LEFT_ALT_PRESSED: UInt = 0x0002u
        const val RIGHT_CTRL_PRESSED: UInt = 0x0004u
        const val LEFT_CTRL_PRESSED: UInt = 0x0008u
        const val SHIFT_PRESSED: UInt = 0x0010u
        val CTRL_PRESSED_MASK = (RIGHT_CTRL_PRESSED or LEFT_CTRL_PRESSED)
        val ALT_PRESSED_MASK = (RIGHT_ALT_PRESSED or LEFT_ALT_PRESSED)

        // https://learn.microsoft.com/en-us/windows/console/mouse-event-record-str
        const val MOUSE_MOVED: UInt = 0x0001u
        const val DOUBLE_CLICK: UInt = 0x0002u
        const val MOUSE_WHEELED: UInt = 0x0004u
        const val MOUSE_HWHEELED: UInt = 0x0008u
        const val FROM_LEFT_1ST_BUTTON_PRESSED: Int = 0x0001
        const val RIGHTMOST_BUTTON_PRESSED: Int = 0x0002
        const val FROM_LEFT_2ND_BUTTON_PRESSED: Int = 0x0004
        const val FROM_LEFT_3RD_BUTTON_PRESSED: Int = 0x0008
        const val FROM_LEFT_4TH_BUTTON_PRESSED: Int = 0x0010


        // https://learn.microsoft.com/en-us/windows/console/setconsolemode
        const val ENABLE_PROCESSED_INPUT = 0x0001u
        const val ENABLE_MOUSE_INPUT = 0x0010u
        const val ENABLE_EXTENDED_FLAGS = 0x0080u
        const val ENABLE_WINDOW_INPUT = 0x0008u
        const val ENABLE_QUICK_EDIT_MODE = 0x0040u
    }

    protected sealed class EventRecord {
        data class Key(
            val bKeyDown: Boolean,
            val wVirtualKeyCode: UShort,
            val uChar: Char,
            val dwControlKeyState: UInt,
        ) : EventRecord()

        data class Mouse(
            val dwMousePositionX: Short,
            val dwMousePositionY: Short,
            val dwButtonState: UInt,
            val dwControlKeyState: UInt,
            val dwEventFlags: UInt,
        ) : EventRecord()
    }

    protected abstract fun readRawEvent(dwMilliseconds: Int): EventRecord?
    protected abstract fun getStdinConsoleMode(): UInt
    protected abstract fun setStdinConsoleMode(dwMode: UInt)

    final override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        val originalMode = getStdinConsoleMode()
        // dwMode=0 means ctrl-c processing, echo, and line input modes are disabled. Could add
        // ENABLE_PROCESSED_INPUT or ENABLE_WINDOW_INPUT if we want those events.
        val dwMode = when (mouseTracking) {
            MouseTracking.Off -> 0u
            else -> ENABLE_MOUSE_INPUT or ENABLE_EXTENDED_FLAGS
        }
        setStdinConsoleMode(dwMode)
        return AutoCloseable { setStdinConsoleMode(originalMode) }
    }

    override fun readInputEvent(timeout: TimeMark, mouseTracking: MouseTracking): InputEvent? {
        val elapsed = timeout.elapsedNow()
        val dwMilliseconds = when {
            elapsed.isInfinite() -> Int.MAX_VALUE
            elapsed.isPositive() -> 0 // positive elapsed is in the past
            else -> -(elapsed.inWholeMilliseconds).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }

        return when (val event = readRawEvent(dwMilliseconds)) {
            null -> null
            is EventRecord.Key -> processKeyEvent(event)
            is EventRecord.Mouse -> processMouseEvent(event, mouseTracking)
        }
    }

    private fun processKeyEvent(event: EventRecord.Key): InputEvent? {
        if (!event.bKeyDown) return null // ignore key up events
        val virtualName = WindowsVirtualKeyCodeToKeyEvent.getName(event.wVirtualKeyCode)
        val shift = event.dwControlKeyState and SHIFT_PRESSED != 0u
        val key = when {
            virtualName != null && virtualName.length == 1 && shift -> {
                if (virtualName[0] in 'a'..'z') virtualName.uppercase()
                else shiftVcodeToKey(virtualName)
            }

            event.uChar in Char.MIN_SURROGATE..Char.MAX_SURROGATE -> {
                // We got a surrogate pair, so we need to read the next char to get the full
                // codepoint. Skip any key up events that might be in the queue.
                var nextEvent: EventRecord?
                do {
                    nextEvent = readRawEvent(0)
                } while (nextEvent != null
                    && (nextEvent !is EventRecord.Key || !nextEvent.bKeyDown)
                )
                if (nextEvent !is EventRecord.Key) {
                    event.uChar.toString()
                } else {
                    charArrayOf(event.uChar, nextEvent.uChar).concatToString()
                }
            }


            virtualName != null -> virtualName
            event.uChar.code != 0 -> event.uChar.toString()
            else -> "Unidentified"
        }
        return KeyboardEvent(
            key = key,
            ctrl = event.dwControlKeyState and CTRL_PRESSED_MASK != 0u,
            alt = event.dwControlKeyState and ALT_PRESSED_MASK != 0u,
            shift = shift,
        )
    }

    private fun processMouseEvent(
        event: EventRecord.Mouse,
        tracking: MouseTracking,
    ): InputEvent? {
        val eventFlags = event.dwEventFlags
        val buttons = event.dwButtonState.toInt()
        if (tracking == MouseTracking.Off
            || tracking == MouseTracking.Normal && eventFlags == MOUSE_MOVED
            || tracking == MouseTracking.Button && eventFlags == MOUSE_MOVED && buttons == 0
        ) return null

        return MouseEvent(
            x = event.dwMousePositionX.toInt(),
            y = event.dwMousePositionY.toInt(),
            left = buttons and FROM_LEFT_1ST_BUTTON_PRESSED != 0,
            right = buttons and RIGHTMOST_BUTTON_PRESSED != 0,
            middle = buttons and FROM_LEFT_2ND_BUTTON_PRESSED != 0,
            mouse4 = buttons and FROM_LEFT_3RD_BUTTON_PRESSED != 0,
            mouse5 = buttons and FROM_LEFT_4TH_BUTTON_PRESSED != 0,
            // If the high word of the dwButtonState member contains a positive value, the wheel
            // was rotated forward, away from the user.
            wheelUp = eventFlags and MOUSE_WHEELED != 0u && buttons shr 16 > 0,
            wheelDown = eventFlags and MOUSE_WHEELED != 0u && buttons shr 16 <= 0,
            // If the high word of the dwButtonState member contains a positive value, the wheel
            // was rotated to the right.
            wheelLeft = eventFlags and MOUSE_HWHEELED != 0u && buttons shr 16 <= 0,
            wheelRight = eventFlags and MOUSE_HWHEELED != 0u && buttons shr 16 > 0,
            ctrl = event.dwControlKeyState and CTRL_PRESSED_MASK != 0u,
            alt = event.dwControlKeyState and ALT_PRESSED_MASK != 0u,
            shift = event.dwControlKeyState and SHIFT_PRESSED != 0u,
        )
    }
}

private fun shiftVcodeToKey(virtualName: String): String {
    return when (virtualName[0]) {
        '1' -> "!"
        '2' -> "@"
        '3' -> "#"
        '4' -> "$"
        '5' -> "%"
        '6' -> "^"
        '7' -> "&"
        '8' -> "*"
        '9' -> "("
        '0' -> ")"
        '-' -> "_"
        '=' -> "+"
        '`' -> "~"
        '[' -> "{"
        ']' -> "}"
        '\\' -> "|"
        ';' -> ":"
        '\'' -> "\""
        ',' -> "<"
        '.' -> ">"
        '/' -> "?"
        else -> virtualName
    }
}
