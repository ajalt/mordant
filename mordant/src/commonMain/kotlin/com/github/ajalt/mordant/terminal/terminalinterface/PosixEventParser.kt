package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.MouseEvent
import com.github.ajalt.mordant.internal.codepointToString
import com.github.ajalt.mordant.internal.readBytesAsUtf8
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val ESC = '\u001b'

/*
  Escape sequences for keys without modifier parameters, derived from combos seen
  at https://github.com/nodejs/node/blob/main/lib/internal/readline/utils.js
*/
private val namedKeys: Map<String, KeyboardEvent> = buildMap {
    fun add(key: String, vararg sequences: String, ctrl: Boolean = false, shift: Boolean = false) {
        for (sequence in sequences) put(sequence, KeyboardEvent(key, ctrl = ctrl, shift = shift))
    }
    add("F1", "[P", "OP", "[11~", "[[A")
    add("F2", "[Q", "OQ", "[12~", "[[B")
    add("F3", "[R", "OR", "[13~", "[[C")
    add("F4", "[S", "OS", "[14~", "[[D")
    add("F5", "[15~", "[[E")
    add("F6", "[17~")
    add("F7", "[18~")
    add("F8", "[19~")
    add("F9", "[20~")
    add("F10", "[21~")
    add("F11", "[23~")
    add("F12", "[24~")
    add("PasteStart", "[200~")
    add("PasteEnd", "[201~")
    add("ArrowUp", "[A", "OA")
    add("ArrowDown", "[B", "OB")
    add("ArrowRight", "[C", "OC")
    add("ArrowLeft", "[D", "OD")
    add("Clear", "[E", "OE")
    add("End", "[F", "OF", "[4~", "[8~")
    add("Home", "[H", "OH", "[1~", "[7~")
    add("Insert", "[2~")
    add("Delete", "[3~")
    add("PageUp", "[5~", "[[5~")
    add("PageDown", "[6~", "[[6~")
    add("Tab", "[Z", shift = true)

    add("ArrowUp", "[a", shift = true)
    add("ArrowDown", "[b", shift = true)
    add("ArrowRight", "[c", shift = true)
    add("ArrowLeft", "[d", shift = true)
    add("Clear", "[e", shift = true)
    add("Insert", "[2$", shift = true)
    add("Delete", "[3$", shift = true)
    add("PageUp", "[5$", shift = true)
    add("PageDown", "[6$", shift = true)
    add("Home", "[7$", shift = true)
    add("End", "[8$", shift = true)

    add("ArrowUp", "Oa", ctrl = true)
    add("ArrowDown", "Ob", ctrl = true)
    add("ArrowRight", "Oc", ctrl = true)
    add("ArrowLeft", "Od", ctrl = true)
    add("Clear", "Oe", ctrl = true)
    add("Insert", "[2^", ctrl = true)
    add("Delete", "[3^", ctrl = true)
    add("PageUp", "[5^", ctrl = true)
    add("PageDown", "[6^", ctrl = true)
    add("Home", "[7^", ctrl = true)
    add("End", "[8^", ctrl = true)
}

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

    fun readInputEvent(timeout: TimeMark): InputEvent? {
        val first = codepointToString(readUtf8Int(timeout) ?: return null)
        if (first.length > 1) return KeyboardEvent(first) // Got a utf8 char like an emoji
        if (first[0] != ESC) return charEvent(first[0], escaped = false)

        // If there's nothing else in the buffer, return "Escape" immediately. This means that
        // you can't manually type in escape sequences, but that's a pretty rare use case
        // compared to just pressing escape.
        val next = readChar(TimeSource.Monotonic.markNow() + 5.milliseconds)
            ?: return KeyboardEvent("Escape")
        return when (next) {
            ESC -> KeyboardEvent("Escape")
            '[', 'O' -> readSequence(introducer = next, timeout = timeout)
            else -> charEvent(next, escaped = true)
        }
    }

    private fun charEvent(ch: Char, escaped: Boolean): KeyboardEvent = when (ch) {
        '\r', '\n' -> KeyboardEvent("Enter", alt = escaped)
        '\t' -> KeyboardEvent("Tab", alt = escaped)
        '\b', '\u007f' -> KeyboardEvent("Backspace", alt = escaped)
        ' ' -> KeyboardEvent(" ", alt = escaped)
        in '\u0000'..'\u001a' if !escaped -> {
            KeyboardEvent(('a' + ch.code - 1).toString(), ctrl = true)
        }

        else if ch.isLetterOrDigit() -> {
            KeyboardEvent(ch.toString(), alt = escaped, shift = ch in 'A'..'Z')
        }

        else if escaped -> KeyboardEvent("Escape", alt = true)
        else -> KeyboardEvent(ch.toString())
    }

    private fun readSequence(introducer: Char, timeout: TimeMark): InputEvent? {
        var prefix = introducer.toString()
        var ch = readChar(timeout) ?: return null
        if (introducer == '[' && ch == '[') {
            prefix = "[["
            ch = readChar(timeout) ?: return null
        }
        // rxvt ends some sequences with `$`, which ECMA-48 defines as an intermediate byte, so
        // the first byte that isn't a parameter is always treated as the final byte.
        val params = StringBuilder()
        while (ch in '0'..'?') {
            params.append(ch)
            ch = readChar(timeout) ?: return null
        }
        return when {
            prefix == "[" && params.isEmpty() && ch == 'M' -> processMouseEvent(timeout)
            prefix == "[" && params.startsWith('<') -> sgrMouseEvent(params.substring(1), ch)
            else -> keyEvent(prefix, params.toString(), ch)
        }
    }

    private fun keyEvent(prefix: String, params: String, final: Char): KeyboardEvent {
        val fields = params.split(';').map { it.substringBefore(':').toIntOrNull() ?: 0 }
        fun field(i: Int) = fields.getOrElse(i) { 0 }
        return when {
            // https://sw.kovidgoyal.net/kitty/keyboard-protocol/
            prefix == "[" && final == 'u' -> codepointEvent(fields[0], modifier = field(1))
            // https://invisible-island.net/xterm/modified-keys.html
            prefix == "[" && final == '~' && fields[0] == 27 -> {
                codepointEvent(field(2), modifier = field(1))
            }

            final in "~^$" -> namedEvent("$prefix${fields[0]}$final", modifier = field(1))
            else -> namedEvent("$prefix$final", modifier = fields.last())
        }
    }

    private fun namedEvent(sequence: String, modifier: Int): KeyboardEvent {
        return (namedKeys[sequence] ?: KeyboardEvent("Unidentified")).withModifier(modifier)
    }

    private fun codepointEvent(codepoint: Int, modifier: Int): KeyboardEvent {
        val key = when (codepoint) {
            8, 127 -> "Backspace"
            9 -> "Tab"
            13 -> "Enter"
            27 -> "Escape"
            in 32..0x10ffff -> codepointToString(codepoint)
            else -> "Unidentified"
        }
        return KeyboardEvent(key).withModifier(modifier)
    }

    private fun KeyboardEvent.withModifier(modifier: Int): KeyboardEvent {
        val bits = (modifier - 1).coerceAtLeast(0)
        return copy(
            ctrl = ctrl || bits and 4 != 0,
            alt = alt || bits and 10 != 0,
            shift = shift || bits and 1 != 0,
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
        // In this encoding, cb == 3 means "a button was released", but there's no way to know
        // which one -_-
        return makeMouseEvent(cb = cb - 32, x = cx, y = cy, release = false)
    }

    // https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Extended-coordinates
    private fun sgrMouseEvent(params: String, final: Char): MouseEvent? {
        if (final != 'M' && final != 'm') return null
        val fields = params.split(';').map { it.toIntOrNull() ?: return null }
        if (fields.size != 3) return null
        val (cb, px, py) = fields
        return makeMouseEvent(cb = cb, x = px - 1, y = py - 1, release = final == 'm')
    }

    private fun makeMouseEvent(cb: Int, x: Int, y: Int, release: Boolean): MouseEvent {
        val button = cb and 3
        val wheel = cb and 64 != 0
        val extra = cb and 128 != 0
        // Button numbers are bit patterns rather than ordinals: buttons 4-7 are the low bits plus
        // 64, and buttons 8-11 are the low bits plus 128. Motion events add 32, and a value of 3
        // in the low bits means no button is involved.
        val pressed = !release && !wheel && !extra && button != 3
        return MouseEvent(
            x = x,
            y = y,
            left = pressed && button == 0,
            middle = pressed && button == 1,
            right = pressed && button == 2,
            mouse4 = !release && extra && button == 0,
            mouse5 = !release && extra && button == 1,
            wheelUp = wheel && button == 0,
            wheelDown = wheel && button == 1,
            wheelLeft = wheel && button == 2,
            wheelRight = wheel && button == 3,
            ctrl = cb and 16 != 0,
            alt = cb and 8 != 0,
            shift = cb and 4 != 0,
        )
    }

    private fun readChar(timeout: TimeMark): Char? = readRawByte(timeout)?.toChar()

    /** Read one utf-8 encoded codepoint from the input stream. */
    private fun readUtf8Int(timeout: TimeMark): Int? {
        return readBytesAsUtf8 { readRawByte(timeout) }
    }
}
