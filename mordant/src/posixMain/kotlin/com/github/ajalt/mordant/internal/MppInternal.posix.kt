@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.internal.KeyboardInputLinux
import com.github.ajalt.mordant.input.internal.PosixRawModeHandler
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

private fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char? = memScoped {
    while (t0.elapsedNow() < timeout) {
        val c = alloc<ByteVar>()
        val read = read(STDIN_FILENO, c.ptr, 1u)
        if (read < 0) return null
        if (read > 0) return c.value.toInt().toChar()
    }
    return null
}

internal actual fun readKeyMpp(timeout: Duration): KeyboardEvent? {
    return KeyboardInputLinux.readKeyEvent(timeout, ::readRawByte)
}

internal actual fun enterRawModeMpp(): AutoCloseable = LinuxNativeRawModeHandler.enterRawMode()

private object LinuxNativeRawModeHandler : PosixRawModeHandler() {
    override fun getStdinTermios(): Termios = memScoped {
        val termios = alloc<termios>()
        tcgetattr(STDIN_FILENO, termios.ptr)
        return Termios(
            iflag = termios.c_iflag,
            oflag = termios.c_oflag,
            cflag = termios.c_cflag,
            lflag = termios.c_lflag,
            cline = termios.c_line.toByte(),
            cc = ByteArray(NCCS) { termios.c_cc[it].toByte() },
            ispeed = termios.c_ispeed,
            ospeed = termios.c_ospeed,
        )
    }

    override fun setStdinTermios(termios: Termios): Unit = memScoped {
        val nativeTermios = alloc<termios>()
        nativeTermios.c_iflag = termios.iflag
        nativeTermios.c_oflag = termios.oflag
        nativeTermios.c_cflag = termios.cflag
        nativeTermios.c_lflag = termios.lflag
        nativeTermios.c_line = termios.cline.toUByte()
        repeat(NCCS) { nativeTermios.c_cc[it] = termios.cc[it].toUByte() }
        nativeTermios.c_ispeed = termios.ispeed
        nativeTermios.c_ospeed = termios.ospeed
        tcsetattr(STDIN_FILENO, TCSADRAIN, nativeTermios.ptr)
    }
}
