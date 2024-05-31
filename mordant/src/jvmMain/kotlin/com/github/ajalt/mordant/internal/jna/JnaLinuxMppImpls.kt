package com.github.ajalt.mordant.internal.jna

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.internal.MppImpls
import com.github.ajalt.mordant.internal.Size
import com.oracle.svm.core.annotate.Delete
import com.sun.jna.*
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

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

private const val IGNBRK: Int = 0x0000001
private const val BRKINT: Int = 0x0000002
private const val IGNPAR: Int = 0x0000004
private const val PARMRK: Int = 0x0000008
private const val INPCK: Int = 0x0000010
private const val ISTRIP: Int = 0x0000020
private const val INLCR: Int = 0x0000040
private const val IGNCR: Int = 0x0000080
private const val ICRNL: Int = 0x0000100
private const val IUCLC: Int = 0x0000200
private const val IXON: Int = 0x0000400
private const val IXANY: Int = 0x0000800
private const val IXOFF: Int = 0x0001000
private const val IMAXBEL: Int = 0x0002000
private const val IUTF8: Int = 0x0004000

private const val OPOST: Int = 0x0000001
private const val OLCUC: Int = 0x0000002
private const val ONLCR: Int = 0x0000004
private const val OCRNL: Int = 0x0000008
private const val ONOCR: Int = 0x0000010
private const val ONLRET: Int = 0x0000020
private const val OFILL: Int = 0x0000040
private const val OFDEL: Int = 0x0000080
private const val NLDLY: Int = 0x0000100
private const val NL0: Int = 0x0000000
private const val NL1: Int = 0x0000100
private const val CRDLY: Int = 0x0000600
private const val CR0: Int = 0x0000000
private const val CR1: Int = 0x0000200
private const val CR2: Int = 0x0000400
private const val CR3: Int = 0x0000600
private const val TABDLY: Int = 0x0001800
private const val TAB0: Int = 0x0000000
private const val TAB1: Int = 0x0000800
private const val TAB2: Int = 0x0001000
private const val TAB3: Int = 0x0001800
private const val XTABS: Int = 0x0001800
private const val BSDLY: Int = 0x0002000
private const val BS0: Int = 0x0000000
private const val BS1: Int = 0x0002000
private const val VTDLY: Int = 0x0004000
private const val VT0: Int = 0x0000000
private const val VT1: Int = 0x0004000
private const val FFDLY: Int = 0x0008000
private const val FF0: Int = 0x0000000
private const val FF1: Int = 0x0008000

private const val CBAUD: Int = 0x000100f
private const val B0: Int = 0x0000000
private const val B50: Int = 0x0000001
private const val B75: Int = 0x0000002
private const val B110: Int = 0x0000003
private const val B134: Int = 0x0000004
private const val B150: Int = 0x0000005
private const val B200: Int = 0x0000006
private const val B300: Int = 0x0000007
private const val B600: Int = 0x0000008
private const val B1200: Int = 0x0000009
private const val B1800: Int = 0x000000a
private const val B2400: Int = 0x000000b
private const val B4800: Int = 0x000000c
private const val B9600: Int = 0x000000d
private const val B19200: Int = 0x000000e
private const val B38400: Int = 0x000000f
private const val EXTA: Int = B19200
private const val EXTB: Int = B38400
private const val CSIZE: Int = 0x0000030
private const val CS5: Int = 0x0000000
private const val CS6: Int = 0x0000010
private const val CS7: Int = 0x0000020
private const val CS8: Int = 0x0000030
private const val CSTOPB: Int = 0x0000040
private const val CREAD: Int = 0x0000080
private const val PARENB: Int = 0x0000100
private const val PARODD: Int = 0x0000200
private const val HUPCL: Int = 0x0000400
private const val CLOCAL: Int = 0x0000800

private const val ISIG: Int = 0x0000001
private const val ICANON: Int = 0x0000002
private const val XCASE: Int = 0x0000004
private const val ECHO: Int = 0x0000008
private const val ECHOE: Int = 0x0000010
private const val ECHOK: Int = 0x0000020
private const val ECHONL: Int = 0x0000040
private const val NOFLSH: Int = 0x0000080
private const val TOSTOP: Int = 0x0000100
private const val ECHOCTL: Int = 0x0000200
private const val ECHOPRT: Int = 0x0000400
private const val ECHOKE: Int = 0x0000800
private const val FLUSHO: Int = 0x0001000
private const val PENDIN: Int = 0x0002000
private const val IEXTEN: Int = 0x0008000
private const val EXTPROC: Int = 0x0010000

private const val TCSANOW: Int = 0x0
private const val TCSADRAIN: Int = 0x1
private const val TCSAFLUSH: Int = 0x2

private const val ESC = '\u001b'

@Delete
@Suppress("ClassName", "PropertyName", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
private interface PosixLibC : Library {

    @Suppress("unused")
    @Structure.FieldOrder("ws_row", "ws_col", "ws_xpixel", "ws_ypixel")
    class winsize : Structure() {
        @JvmField
        var ws_row: Short = 0

        @JvmField
        var ws_col: Short = 0

        @JvmField
        var ws_xpixel: Short = 0

        @JvmField
        var ws_ypixel: Short = 0
    }

    @Structure.FieldOrder(
        "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed", "c_ospeed"
    )
    class termios : Structure() {
        @JvmField
        var c_iflag: Int = 0

        @JvmField
        var c_oflag: Int = 0

        @JvmField
        var c_cflag: Int = 0

        @JvmField
        var c_lflag: Int = 0

        @JvmField
        var c_line: Byte = 0

        @JvmField
        var c_cc: ByteArray = ByteArray(32)

        @JvmField
        var c_ispeed: Int = 0

        @JvmField
        var c_ospeed: Int = 0
    }


    fun isatty(fd: Int): Int
    fun ioctl(fd: Int, cmd: Int, data: winsize?): Int

    @Throws(LastErrorException::class)
    fun tcgetattr(fd: Int, termios: termios)

    @Throws(LastErrorException::class)
    fun tcsetattr(fd: Int, cmd: Int, termios: termios)
}

@Delete
internal class JnaLinuxMppImpls : MppImpls {
    @Suppress("SpellCheckingInspection")
    private companion object {
        private const val STDIN_FILENO = 0
        private const val STDOUT_FILENO = 1
        private const val STDERR_FILENO = 2

        private const val TIOCGWINSZ = 0x00005413
    }

    private val libC: PosixLibC = Native.load(Platform.C_LIBRARY_NAME, PosixLibC::class.java)
    override fun stdoutInteractive(): Boolean = libC.isatty(STDOUT_FILENO) == 1
    override fun stdinInteractive(): Boolean = libC.isatty(STDIN_FILENO) == 1
    override fun stderrInteractive(): Boolean = libC.isatty(STDERR_FILENO) == 1

    override fun getTerminalSize(): Size? {
        val size = PosixLibC.winsize()
        return if (libC.ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    // https://www.man7.org/linux/man-pages/man3/termios.3.html
    // https://viewsourcecode.org/snaptoken/kilo/02.enteringRawMode.html
    fun enterRawMode(): AutoCloseable {
        val originalTermios = PosixLibC.termios()
        val termios = PosixLibC.termios()
        libC.tcgetattr(STDIN_FILENO, originalTermios)
        libC.tcgetattr(STDIN_FILENO, termios)
        // we leave OPOST on so we don't change \r\n handling
        termios.c_iflag = termios.c_iflag and (ICRNL or INPCK or ISTRIP or IXON).inv()
        termios.c_cflag = termios.c_cflag or CS8
        termios.c_lflag = termios.c_lflag and (ECHO or ICANON or IEXTEN or ISIG).inv()
        termios.c_cc[VMIN] = 0 // min wait time on read
        termios.c_cc[VTIME] = 1 // max wait time on read, in 10ths of a second
        libC.tcsetattr(STDIN_FILENO, TCSADRAIN, termios)
        return AutoCloseable { libC.tcsetattr(STDIN_FILENO, TCSADRAIN, originalTermios) }
    }

    private fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char? {
        while (t0.elapsedNow() < timeout) {
            val c = System.`in`.read().takeIf { it >= 0 }?.toChar()
            if (c != null) return c
        }
        return null
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
    fun readKeyEvent(timeout: Duration = Duration.INFINITE): KeyboardEvent? {
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
                code = "Escape",
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
            TODO()
//            name = ch.length ? undefined : "escape"
//            alt = true
        }

        return KeyboardEvent(
            key = name ?: ch.toString(),
            code = name ?: ch.toString(),
            ctrl = ctrl,
            alt = alt,
            shift = shift,
        )
    }
}
