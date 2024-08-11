package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.rendering.Size
import com.sun.jna.*
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("ClassName", "PropertyName", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
private interface MacosLibC : Library {

    @Suppress("unused")
    class winsize : Structure() {
        @JvmField
        var ws_row: Short = 0

        @JvmField
        var ws_col: Short = 0

        @JvmField
        var ws_xpixel: Short = 0

        @JvmField
        var ws_ypixel: Short = 0

        override fun getFieldOrder(): List<String> {
            return mutableListOf("ws_row", "ws_col", "ws_xpixel", "ws_ypixel")
        }
    }

    @Structure.FieldOrder(
        "c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed"
    )
    class termios : Structure() {
        @JvmField
        var c_iflag: NativeLong = NativeLong()

        @JvmField
        var c_oflag: NativeLong = NativeLong()

        @JvmField
        var c_cflag: NativeLong = NativeLong()

        @JvmField
        var c_lflag: NativeLong = NativeLong()

        @JvmField
        var c_cc: ByteArray = ByteArray(20)

        @JvmField
        var c_ispeed: NativeLong = NativeLong()

        @JvmField
        var c_ospeed: NativeLong = NativeLong()
    }

    fun isatty(fd: Int): Int
    fun ioctl(fd: Int, cmd: NativeLong?, data: winsize?): Int

    @Throws(LastErrorException::class)
    fun tcgetattr(fd: Int, termios: termios)

    @Throws(LastErrorException::class)
    fun tcsetattr(fd: Int, cmd: Int, termios: termios)
}

@Suppress("SpellCheckingInspection")
internal class TerminalInterfaceJnaMacos : TerminalInterfaceJvmPosix() {
    override val termiosConstants: TermiosConstants get() = MacosTermiosConstants
    private val TCSANOW: Int = 0x0
    private val TIOCGWINSZ = when {
        Platform.isMIPS() || Platform.isPPC() || Platform.isSPARC() -> 0x40087468L
        else -> 0x00005413L
    }

    private val libC: MacosLibC = Native.load(Platform.C_LIBRARY_NAME, MacosLibC::class.java)
    override fun isatty(fd: Int): Boolean = libC.isatty(fd) != 0

    override fun getTerminalSize(): Size? {
        // XXX: JNA has a bug that causes this to fail on macosArm64, use stty on mac for now
        // val size = MacosLibC.winsize()
        // return if (libC.ioctl(STDIN_FILENO, NativeLong(TIOCGWINSZ), size) < 0) {
        //     null
        // } else {
        //     size.ws_col.toInt() to size.ws_row.toInt()
        // }
        return getSttySize(100)
    }

    override fun getStdinTermios(): Termios {
        val termios = MacosLibC.termios()
        libC.tcgetattr(STDIN_FILENO, termios)
        return Termios(
            iflag = termios.c_iflag.toInt().toUInt(),
            oflag = termios.c_oflag.toInt().toUInt(),
            cflag = termios.c_cflag.toInt().toUInt(),
            lflag = termios.c_lflag.toInt().toUInt(),
            cc = termios.c_cc.copyOf(),
        )
    }

    override fun setStdinTermios(termios: Termios) {
        val nativeTermios = MacosLibC.termios()
        libC.tcgetattr(STDIN_FILENO, nativeTermios)
        nativeTermios.c_iflag.setValue(termios.iflag.toLong())
        nativeTermios.c_oflag.setValue(termios.oflag.toLong())
        nativeTermios.c_cflag.setValue(termios.cflag.toLong())
        nativeTermios.c_lflag.setValue(termios.lflag.toLong())
        termios.cc.copyInto(nativeTermios.c_cc)
        libC.tcsetattr(STDIN_FILENO, TCSANOW, nativeTermios)
    }

    override fun shouldAutoUpdateSize(): Boolean {
        return false // Shelling out to STTY is slow, so don't do it automatically
    }
}

@Suppress("SameParameterValue")
private fun getSttySize(timeoutMs: Long): Size? {
    val process = when {
        // Try running stty both directly and via env, since neither one works on all systems
        else -> runCommand("stty", "size") ?: runCommand("/usr/bin/env", "stty", "size")
    } ?: return null
    try {
        if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
            return null
        }
    } catch (e: InterruptedException) {
        return null
    }

    val output = process.inputStream.bufferedReader().readText().trim()
    return when {
        else -> parseSttySize(output)
    }
}

private fun runCommand(vararg args: String): Process? {
    return try {
        ProcessBuilder(*args)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()
    } catch (e: IOException) {
        null
    }
}

private fun parseSttySize(output: String): Size? {
    val dimens = output.split(" ").mapNotNull { it.toIntOrNull() }
    if (dimens.size != 2) return null
    return Size(width = dimens[1], height = dimens[0])
}
