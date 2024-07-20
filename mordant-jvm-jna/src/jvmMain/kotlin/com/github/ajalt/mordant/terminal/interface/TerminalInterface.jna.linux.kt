package com.github.ajalt.mordant.terminal.`interface`

import com.github.ajalt.mordant.rendering.Size
import com.sun.jna.*

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

internal object TerminalInterfaceJnaLinux : TerminalInterfaceJvmPosix() {
    private const val TIOCGWINSZ = 0x00005413
    private const val TCSADRAIN: Int = 0x1
    override val termiosConstants: TermiosConstants get() = LinuxTermiosConstants
    private val libC: PosixLibC = Native.load(Platform.C_LIBRARY_NAME, PosixLibC::class.java)
    override fun isatty(fd: Int): Boolean = libC.isatty(fd) != 0

    override fun getTerminalSize(): Size? {
        val size = PosixLibC.winsize()
        return if (libC.ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios {
        val termios = PosixLibC.termios()
        libC.tcgetattr(STDIN_FILENO, termios)
        return Termios(
            iflag = termios.c_iflag.toUInt(),
            oflag = termios.c_oflag.toUInt(),
            cflag = termios.c_cflag.toUInt(),
            lflag = termios.c_lflag.toUInt(),
            cc = termios.c_cc.copyOf(),
        )
    }

    override fun setStdinTermios(termios: Termios) {
        val nativeTermios = PosixLibC.termios()
        libC.tcgetattr(STDIN_FILENO, nativeTermios)
        nativeTermios.c_iflag = termios.iflag.toInt()
        nativeTermios.c_oflag = termios.oflag.toInt()
        nativeTermios.c_cflag = termios.cflag.toInt()
        nativeTermios.c_lflag = termios.lflag.toInt()
        termios.cc.copyInto(nativeTermios.c_cc)
        libC.tcsetattr(STDIN_FILENO, TCSADRAIN, nativeTermios)
    }
}
