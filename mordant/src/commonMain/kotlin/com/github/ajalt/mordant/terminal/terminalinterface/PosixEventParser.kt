package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.MouseEvent
import com.github.ajalt.mordant.internal.codepointToString
import com.github.ajalt.mordant.internal.readBytesAsUtf8
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val ESC = '\u001b'


internal class PosixEventParser(
    private val readRawByte: () -> Int?,
) {
    private fun readRawByte(timeout: TimeMark): Int? {
        do {
            val byte = readRawByte()
            if (byte != null) return byte
        } while (timeout.hasNotPassedNow())
        return null
    }

    /*
      Some patterns seen in terminal key escape codes, derived from combos seen
      at https://github.com/nodejs/node/blob/main/lib/internal/readline/utils.js
    */
    fun readInputEvent(timeout: TimeMark): InputEvent? {
        var ctrl = false
        var alt = false
        var shift = false
        var escaped = false
        var name: String? = null
        val s = StringBuilder()
        var ch: Char

        fun read(t:TimeMark = timeout): Char? {
            ch = readRawByte(t)?.toChar() ?: return null
            s.append(ch)
            return ch
        }

        val first = codepointToString(readUtf8Int(timeout) ?: return null)
        if (first.length > 1) {
            return KeyboardEvent(first) // Got a utf8 char like an emoji
        } else {
            ch = first[0]
        }

        if (ch == ESC) {
            escaped = true
            // If there's nothing else in the buffer, return "Escape" immediately. This means that
            // you can't manually type in escape sequences, but that's a pretty rare use case
            // compared to just pressing escape.
            read(TimeSource.Monotonic.markNow() + 5.milliseconds) ?: return KeyboardEvent("Escape")
            if (ch == ESC) {
                return KeyboardEvent("Escape")
            }
        }

        if (escaped && (ch == 'O' || ch == '[')) {
            // ANSI escape sequence
            val code = StringBuilder(ch.toString())
            var modifier = 0

            if (ch == 'O') {
                // ESC O letter
                // ESC O modifier letter
                read() ?: return null

                if (ch in '0'..'9') {
                    modifier = ch.code - 1
                    read() ?: return null
                }

                code.append(ch)
            } else if (ch == '[') {
                // ESC [ letter
                // ESC [ modifier letter
                // ESC [ [ modifier letter
                // ESC [ [ num char
                // For mouse events:
                // ESC [ M byte byte byte
                read() ?: return null

                if (ch == '[') {
                    // escape codes might have a second bracket
                    code.append(ch)
                    read() ?: return null
                } else if (ch == 'M') {
                    // mouse event
                    return processMouseEvent(timeout)
                }

                val cmdStart = s.length - 1

                // leading digits
                repeat(3) {
                    if (ch in '0'..'9') {
                        read() ?: return null
                    }
                }

                // modifier
                if (ch == ';') {
                    read() ?: return null

                    if (ch in '0'..'9') {
                        read() ?: return null
                    }
                }

                // We buffered enough data, now extract code and modifier from it
                val cmd = s.substring(cmdStart)
                var match = Regex("""(\d\d?)(?:;(\d))?([~^$])|(\d{3}~)""").matchEntire(cmd)
                if (match != null) {
                    if (match.groupValues[4].isNotEmpty()) {
                        code.append(match.groupValues[4])
                    } else {
                        code.append(match.groupValues[1] + match.groupValues[3])
                        modifier = (match.groupValues[2].toIntOrNull() ?: 1) - 1
                    }
                } else {
                    match = Regex("""((\d;)?(\d))?([A-Za-z])""").matchEntire(cmd)
                    if (match != null) {
                        code.append(match.groupValues[4])
                        modifier = (match.groupValues[3].toIntOrNull() ?: 1) - 1
                    } else {
                        code.append(cmd)
                    }
                }
            }

            // Parse the key modifier
            ctrl = (modifier and 4) != 0
            alt = (modifier and 10) != 0
            shift = (modifier and 1) != 0

            when (code.toString()) {
                "[P" -> name = "F1"
                "[Q" -> name = "F2"
                "[R" -> name = "F3"
                "[S" -> name = "F4"
                "OP" -> name = "F1"
                "OQ" -> name = "F2"
                "OR" -> name = "F3"
                "OS" -> name = "F4"
                "[11~" -> name = "F1"
                "[12~" -> name = "F2"
                "[13~" -> name = "F3"
                "[14~" -> name = "F4"
                "[200~" -> name = "PasteStart"
                "[201~" -> name = "PasteEnd"
                "[[A" -> name = "F1"
                "[[B" -> name = "F2"
                "[[C" -> name = "F3"
                "[[D" -> name = "F4"
                "[[E" -> name = "F5"
                "[15~" -> name = "F5"
                "[17~" -> name = "F6"
                "[18~" -> name = "F7"
                "[19~" -> name = "F8"
                "[20~" -> name = "F9"
                "[21~" -> name = "F10"
                "[23~" -> name = "F11"
                "[24~" -> name = "F12"
                "[A" -> name = "ArrowUp"
                "[B" -> name = "ArrowDown"
                "[C" -> name = "ArrowRight"
                "[D" -> name = "ArrowLeft"
                "[E" -> name = "Clear"
                "[F" -> name = "End"
                "[H" -> name = "Home"
                "OA" -> name = "ArrowUp"
                "OB" -> name = "ArrowDown"
                "OC" -> name = "ArrowRight"
                "OD" -> name = "ArrowLeft"
                "OE" -> name = "Clear"
                "OF" -> name = "End"
                "OH" -> name = "Home"
                "[1~" -> name = "Home"
                "[2~" -> name = "Insert"
                "[3~" -> name = "Delete"
                "[4~" -> name = "End"
                "[5~" -> name = "PageUp"
                "[6~" -> name = "PageDown"
                "[[5~" -> name = "PageUp"
                "[[6~" -> name = "PageDown"
                "[7~" -> name = "Home"
                "[8~" -> name = "End"
                "[a" -> {
                    name = "ArrowUp"
                    shift = true
                }

                "[b" -> {
                    name = "ArrowDown"
                    shift = true
                }

                "[c" -> {
                    name = "ArrowRight"
                    shift = true
                }

                "[d" -> {
                    name = "ArrowLeft"
                    shift = true
                }

                "[e" -> {
                    name = "Clear"
                    shift = true
                }

                "[2$" -> {
                    name = "Insert"
                    shift = true
                }

                "[3$" -> {
                    name = "Delete"
                    shift = true
                }

                "[5$" -> {
                    name = "PageUp"
                    shift = true
                }

                "[6$" -> {
                    name = "PageDown"
                    shift = true
                }

                "[7$" -> {
                    name = "Home"
                    shift = true
                }

                "[8$" -> {
                    name = "End"
                    shift = true
                }

                "Oa" -> {
                    name = "ArrowUp"
                    ctrl = true
                }

                "Ob" -> {
                    name = "ArrowDown"
                    ctrl = true
                }

                "Oc" -> {
                    name = "ArrowRight"
                    ctrl = true
                }

                "Od" -> {
                    name = "ArrowLeft"
                    ctrl = true
                }

                "Oe" -> {
                    name = "Clear"
                    ctrl = true
                }

                "[2^" -> {
                    name = "Insert"
                    ctrl = true
                }

                "[3^" -> {
                    name = "Delete"
                    ctrl = true
                }

                "[5^" -> {
                    name = "PageUp"
                    ctrl = true
                }

                "[6^" -> {
                    name = "PageDown"
                    ctrl = true
                }

                "[7^" -> {
                    name = "Home"
                    ctrl = true
                }

                "[8^" -> {
                    name = "End"
                    ctrl = true
                }

                "[Z" -> {
                    name = "Tab"
                    shift = true
                }

                else -> name = "Unidentified"
            }
        } else if (ch == '\r') {
            name = "Enter"
            alt = escaped
        } else if (ch == '\n') {
            name = "Enter"
            alt = escaped
        } else if (ch == '\t') {
            name = "Tab"
            alt = escaped
        } else if (ch == '\b' || ch == '\u007f') {
            // backspace or ctrl+h
            name = "Backspace"
            alt = escaped
        } else if (ch == ESC) {
            // escape key
            name = "Escape"
            alt = escaped
        } else if (ch == ' ') {
            name = " "
            alt = escaped
        } else if (!escaped && ch <= '\u001a') {
            // ctrl+letter
            name = (ch.code + 'a'.code - 1).toChar().toString()
            ctrl = true
        } else if (ch.isLetter() || ch.isDigit()) {
            // Letter, number, shift+letter
            name = ch.toString()
            shift = ch in 'A'..'Z'
            alt = escaped
        } else if (escaped) {
            // Escape sequence timeout
            if (name == null) name = "Escape"
            alt = true
        }

        return KeyboardEvent(
            key = name ?: ch.toString(),
            ctrl = ctrl,
            alt = alt,
            shift = shift,
        )
    }

    private fun processMouseEvent(timeout: TimeMark): InputEvent? {
        // Mouse event coordinates are raw values, not decimal text, and they're sometimes utf-8
        // encoded to fit larger values.
        val cb = readUtf8Int(timeout) ?: return null
        val cx = (readUtf8Int(timeout) ?: return null) - 33
        // XXX: I've seen the terminal not send the third byte like `ESC [ M # W`, but I can't find
        // that pattern documented anywhere, so maybe it's an issue with the terminal emulator not
        // encoding utf8 correctly?
        val cy = readUtf8Int(TimeSource.Monotonic.markNow() + 1.milliseconds).let {
            if (it == null) 0 else it - 33
        }
        val shift = (cb and 4) != 0
        val alt = (cb and 8) != 0
        val ctrl = (cb and 16) != 0
        // cb == 3 means "a button was released", but there's no way to know which one -_-
        return MouseEvent(
            x = cx,
            y = cy,
            // On button-motion events, xterm adds 32 to cb
            left = cb and 3 == 0,
            right = cb and 3 == 1,
            middle = cb and 3 == 2,
            wheelUp = cb == 64,
            wheelDown = cb == 65,
            ctrl = ctrl,
            alt = alt,
            shift = shift,
        )
    }

    /** Read one utf-8 encoded codepoint from the input stream. */
    private fun readUtf8Int(timeout: TimeMark): Int? {
        return readBytesAsUtf8 { readRawByte(timeout) }
    }
}
