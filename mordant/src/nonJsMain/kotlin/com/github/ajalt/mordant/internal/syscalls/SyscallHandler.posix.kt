package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.KeyboardEvent
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

internal abstract class SyscallHandlerPosix : SyscallHandler {
    protected companion object {
        const val STDIN_FILENO = 0
        const val STDOUT_FILENO = 1
        const val STDERR_FILENO = 2

        private const val ESC = '\u001b'

        private const val VINTR: Int = 0
        private const val VQUIT: Int = 1
        private const val VERASE: Int = 2
        private const val VKILL: Int = 3
        private const val VEOF: Int = 4
        private const val VTIME: Int = 5
        private const val VMIN: Int = 6
        private const val VSWTC: Int = 7
        private const val VSTART: Int = 8
        private const val VSTOP: Int = 9
        private const val VSUSP: Int = 10
        private const val VEOL: Int = 11
        private const val VREPRINT: Int = 12
        private const val VDISCARD: Int = 13
        private const val VWERASE: Int = 14
        private const val VLNEXT: Int = 15
        private const val VEOL2: Int = 16

        private const val IGNBRK: UInt = 0x0000001u
        private const val BRKINT: UInt = 0x0000002u
        private const val IGNPAR: UInt = 0x0000004u
        private const val PARMRK: UInt = 0x0000008u
        private const val INPCK: UInt = 0x0000010u
        private const val ISTRIP: UInt = 0x0000020u
        private const val INLCR: UInt = 0x0000040u
        private const val IGNCR: UInt = 0x0000080u
        private const val ICRNL: UInt = 0x0000100u
        private const val IUCLC: UInt = 0x0000200u
        private const val IXON: UInt = 0x0000400u
        private const val IXANY: UInt = 0x0000800u
        private const val IXOFF: UInt = 0x0001000u
        private const val IMAXBEL: UInt = 0x0002000u
        private const val IUTF8: UInt = 0x0004000u

        private const val OPOST: UInt = 0x0000001u
        private const val OLCUC: UInt = 0x0000002u
        private const val ONLCR: UInt = 0x0000004u
        private const val OCRNL: UInt = 0x0000008u
        private const val ONOCR: UInt = 0x0000010u
        private const val ONLRET: UInt = 0x0000020u
        private const val OFILL: UInt = 0x0000040u
        private const val OFDEL: UInt = 0x0000080u
        private const val NLDLY: UInt = 0x0000100u
        private const val NL0: UInt = 0x0000000u
        private const val NL1: UInt = 0x0000100u
        private const val CRDLY: UInt = 0x0000600u
        private const val CR0: UInt = 0x0000000u
        private const val CR1: UInt = 0x0000200u
        private const val CR2: UInt = 0x0000400u
        private const val CR3: UInt = 0x0000600u
        private const val TABDLY: UInt = 0x0001800u
        private const val TAB0: UInt = 0x0000000u
        private const val TAB1: UInt = 0x0000800u
        private const val TAB2: UInt = 0x0001000u
        private const val TAB3: UInt = 0x0001800u
        private const val XTABS: UInt = 0x0001800u
        private const val BSDLY: UInt = 0x0002000u
        private const val BS0: UInt = 0x0000000u
        private const val BS1: UInt = 0x0002000u
        private const val VTDLY: UInt = 0x0004000u
        private const val VT0: UInt = 0x0000000u
        private const val VT1: UInt = 0x0004000u
        private const val FFDLY: UInt = 0x0008000u
        private const val FF0: UInt = 0x0000000u
        private const val FF1: UInt = 0x0008000u

        private const val CBAUD: UInt = 0x000100fu
        private const val B0: UInt = 0x0000000u
        private const val B50: UInt = 0x0000001u
        private const val B75: UInt = 0x0000002u
        private const val B110: UInt = 0x0000003u
        private const val B134: UInt = 0x0000004u
        private const val B150: UInt = 0x0000005u
        private const val B200: UInt = 0x0000006u
        private const val B300: UInt = 0x0000007u
        private const val B600: UInt = 0x0000008u
        private const val B1200: UInt = 0x0000009u
        private const val B1800: UInt = 0x000000au
        private const val B2400: UInt = 0x000000bu
        private const val B4800: UInt = 0x000000cu
        private const val B9600: UInt = 0x000000du
        private const val B19200: UInt = 0x000000eu
        private const val B38400: UInt = 0x000000fu
        private const val EXTA: UInt = B19200
        private const val EXTB: UInt = B38400
        private const val CSIZE: UInt = 0x0000030u
        private const val CS5: UInt = 0x0000000u
        private const val CS6: UInt = 0x0000010u
        private const val CS7: UInt = 0x0000020u
        private const val CS8: UInt = 0x0000030u
        private const val CSTOPB: UInt = 0x0000040u
        private const val CREAD: UInt = 0x0000080u
        private const val PARENB: UInt = 0x0000100u
        private const val PARODD: UInt = 0x0000200u
        private const val HUPCL: UInt = 0x0000400u
        private const val CLOCAL: UInt = 0x0000800u

        private const val ISIG: UInt = 0x0000001u
        private const val ICANON: UInt = 0x0000002u
        private const val XCASE: UInt = 0x0000004u
        private const val ECHO: UInt = 0x0000008u
        private const val ECHOE: UInt = 0x0000010u
        private const val ECHOK: UInt = 0x0000020u
        private const val ECHONL: UInt = 0x0000040u
        private const val NOFLSH: UInt = 0x0000080u
        private const val TOSTOP: UInt = 0x0000100u
        private const val ECHOCTL: UInt = 0x0000200u
        private const val ECHOPRT: UInt = 0x0000400u
        private const val ECHOKE: UInt = 0x0000800u
        private const val FLUSHO: UInt = 0x0001000u
        private const val PENDIN: UInt = 0x0002000u
        private const val IEXTEN: UInt = 0x0008000u
        private const val EXTPROC: UInt = 0x0010000u

        private const val TCSANOW: UInt = 0x0u
        private const val TCSADRAIN: UInt = 0x1u
        private const val TCSAFLUSH: UInt = 0x2u
    }

    data class Termios(
        val iflag: UInt,
        val oflag: UInt,
        val cflag: UInt,
        val lflag: UInt,
        val cline: Byte,
        val cc: ByteArray,
        val ispeed: UInt,
        val ospeed: UInt,
    )

    abstract fun getStdinTermios(): Termios?
    abstract fun setStdinTermios(termios: Termios)
    protected abstract fun isatty(fd: Int): Boolean
    protected abstract fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char?

    override fun stdoutInteractive(): Boolean = isatty(STDOUT_FILENO)
    override fun stdinInteractive(): Boolean = isatty(STDIN_FILENO)
    override fun stderrInteractive(): Boolean = isatty(STDERR_FILENO)

    // https://www.man7.org/linux/man-pages/man3/termios.3.html
    // https://viewsourcecode.org/snaptoken/kilo/02.enteringRawMode.html
    override fun enterRawMode(): AutoCloseable? {
        val orig = getStdinTermios() ?: return null
        val new = Termios(
            iflag = orig.iflag and (ICRNL or IGNCR or INPCK or ISTRIP or IXON).inv(),
            // we leave OPOST on so we don't change \r\n handling
            oflag = orig.oflag,
            cflag = orig.cflag or CS8,
            lflag = orig.lflag and (ECHO or ICANON or IEXTEN or ISIG).inv(),
            cline = orig.cline,
            cc = orig.cc.copyOf().also {
                it[VMIN] = 0 // min wait time on read
                it[VTIME] = 1 // max wait time on read, in 10ths of a second
            },
            ispeed = orig.ispeed,
            ospeed = orig.ospeed,
        )
        setStdinTermios(new)
        return AutoCloseable { setStdinTermios(orig) }
    }

    /*
      Some patterns seen in terminal key escape codes, derived from combos seen
      at https://github.com/nodejs/node/blob/main/lib/internal/readline/utils.js

      ESC letter
      ESC [ letter
      ESC [ modifier letter
      ESC [ 1 ; modifier letter
      ESC [ num char
      ESC [ num ; modifier char
      ESC O letter
      ESC O modifier letter
      ESC O 1 ; modifier letter
      ESC N letter
      ESC [ [ num ; modifier char
      ESC [ [ 1 ; modifier letter
      ESC ESC [ num char
      ESC ESC O letter

      - char is usually ~ but $ and ^ also happen with rxvt
      - modifier is 1 +
                    (shift     * 1) +
                    (left_alt  * 2) +
                    (ctrl      * 4) +
                    (right_alt * 8)
      - two leading ESCs apparently mean the same as one leading ESC
    */
    override fun readKeyEvent(timeout: Duration): KeyboardEvent? {
        val t0 = TimeSource.Monotonic.markNow()
        var ctrl = false
        var alt = false
        var shift = false
        var escaped = false
        var name: String? = null
        val s = StringBuilder()
        var ch: Char = ' '

        fun readTimeout(): Boolean {
            ch = readRawByte(t0, timeout) ?: return true
            s.append(ch)
            return false
        }

        if (readTimeout()) return null

        if (ch == ESC) {
            escaped = true
            if (readTimeout()) return KeyboardEvent(
                key = "Escape",
                ctrl = false,
                alt = false,
                shift = false
            )
            if (ch == ESC) readTimeout()
        }

        if (escaped && (ch == 'O' || ch == '[')) {
            // ANSI escape sequence
            val code = StringBuilder(ch.toString())
            var modifier = 0

            if (ch == 'O') {
                // ESC O letter
                // ESC O modifier letter
                if (readTimeout()) return null

                if (ch in '0'..'9') {
                    modifier = ch.code - 1
                    if (readTimeout()) return null
                }

                code.append(ch)
            } else if (ch == '[') {
                // ESC [ letter
                // ESC [ modifier letter
                // ESC [ [ modifier letter
                // ESC [ [ num char
                if (readTimeout()) return null

                if (ch == '[') {
                    // escape codes might have a second bracket
                    code.append(ch)
                    if (readTimeout()) return null
                }

                /*
                 * Here and later we try to buffer just enough data to get
                 * a complete ascii sequence.
                 *
                 * We have basically two classes of ascii characters to process:
                 *
                 *
                 * 1. `\x1b[24;5~` should be parsed as { code: '[24~', modifier: 5 }
                 *
                 * This particular example is featuring Ctrl+F12 in xterm.
                 *
                 *  - `;5` part is optional, e.g. it could be `\x1b[24~`
                 *  - first part can contain one or two digits
                 *  - there is also special case when there can be 3 digits
                 *    but without modifier. They are the case of paste bracket mode
                 *
                 * So the generic regexp is like /^(?:\d\d?(;\d)?[~^$]|\d{3}~)$/
                 *
                 *
                 * 2. `\x1b[1;5H` should be parsed as { code: '[H', modifier: 5 }
                 *
                 * This particular example is featuring Ctrl+Home in xterm.
                 *
                 *  - `1;5` part is optional, e.g. it could be `\x1b[H`
                 *  - `1;` part is optional, e.g. it could be `\x1b[5H`
                 *
                 * So the generic regexp is like /^((\d;)?\d)?[A-Za-z]$/
                 *
                 */
                val cmdStart = s.length - 1

                // leading digits
                repeat(3) {
                    if (ch in '0'..'9') {
                        if (readTimeout()) return null
                    }
                }

                // modifier
                if (ch == ';') {
                    if (readTimeout()) return null

                    if (ch in '0'..'9') {
                        if (readTimeout()) return null
                    }
                }

                /*
                 * We buffered enough data, now trying to extract code
                 * and modifier from it
                 */
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
            TODO("Escape sequence timeout")
//            name = ch.length ? undefined : "escape"
//            alt = true
        }

        return KeyboardEvent(
            key = name ?: ch.toString(),
            ctrl = ctrl,
            alt = alt,
            shift = shift,
        )
    }
}

