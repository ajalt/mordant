package com.github.ajalt.mordant.internal.jna

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.internal.KeyboardInputLinux
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
    override fun enterRawMode(): AutoCloseable {
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

    override fun readKey(timeout: Duration): KeyboardEvent? {
        return KeyboardInputLinux.readKeyEvent(timeout, ::readRawByte)
    }
}
