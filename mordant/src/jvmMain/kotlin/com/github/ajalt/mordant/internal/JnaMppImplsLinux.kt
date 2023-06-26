package com.github.ajalt.mordant.internal

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Structure

@Suppress("ClassName", "PropertyName", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
interface PosixLibC : Library {

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

    fun isatty(fd: Int): Int
    fun ioctl(fd: Int, cmd: Int, data: winsize?): Int
}

internal class LinuxMppImpls : JnaMppImpls {
    @Suppress("SpellCheckingInspection")
    private companion object {
        const val STDIN_FILENO = 0
        const val STDOUT_FILENO = 1
        const val STDERR_FILENO = 2

        const val TIOCGWINSZ = 0x00005413
    }

    private val libC: PosixLibC = Native.load(Platform.C_LIBRARY_NAME, PosixLibC::class.java)
    override fun stdoutInteractive(): Boolean = libC.isatty(STDOUT_FILENO) == 1
    override fun stdinInteractive(): Boolean = libC.isatty(STDIN_FILENO) == 1
    override fun stderrInteractive(): Boolean = libC.isatty(STDERR_FILENO) == 1

    override fun getTerminalSize(): Pair<Int, Int>? {
        val size = PosixLibC.winsize()
        return if (libC.ioctl(STDIN_FILENO, TIOCGWINSZ, size) < 0) {
            null
        } else {
            size.ws_col.toInt() to size.ws_row.toInt()
        }
    }
}
