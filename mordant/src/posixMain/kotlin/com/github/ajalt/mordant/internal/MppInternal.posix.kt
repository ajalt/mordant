@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.internal.KeyboardInputLinux
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

internal actual fun enterRawModeMpp(): AutoCloseable = memScoped {
    val termios = alloc<termios>()
    tcgetattr(STDIN_FILENO, termios.ptr)

    // This is clunky, but K/N doesn't seem to have a way to copy a struct, and calling tcgetattr
    // twice here doesn't work because the kernel seems to mutate the returned values later.
    val originalTermios = cValue<termios> {
        repeat(NCCS) { c_cc[it] = termios.c_cc[it] }
        c_cflag = termios.c_cflag
        c_iflag = termios.c_iflag
        c_ispeed = termios.c_ispeed
        c_lflag = termios.c_lflag
        c_line = termios.c_line
        c_oflag = termios.c_oflag
        c_ospeed = termios.c_ospeed
    }

    // we leave OPOST on so we don't change \r\n handling
    termios.c_iflag = termios.c_iflag and (ICRNL or INPCK or ISTRIP or IXON).inv().toUInt()
    termios.c_cflag = termios.c_cflag or CS8.toUInt()
    termios.c_lflag = termios.c_lflag and (ECHO or ICANON or IEXTEN or ISIG).inv().toUInt()
    termios.c_cc[VMIN] = 0u // min wait time on read
    termios.c_cc[VTIME] = 1u // max wait time on read, in 10ths of a second
    tcsetattr(STDIN_FILENO, TCSADRAIN, termios.ptr)
    return AutoCloseable { tcsetattr(STDIN_FILENO, TCSADRAIN, originalTermios.ptr) }
}
