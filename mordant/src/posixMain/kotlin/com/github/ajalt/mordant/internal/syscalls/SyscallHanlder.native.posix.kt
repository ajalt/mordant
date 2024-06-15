package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.internal.Size
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

internal object SyscallHandlerNativePosix : SyscallHandlerPosix() {
    override fun isatty(fd: Int): Boolean {
        return platform.posix.isatty(fd) != 0
    }

    override fun getTerminalSize(): Size? = memScoped {
        val size = alloc<winsize>()
        if (ioctl(platform.posix.STDIN_FILENO, TIOCGWINSZ.toULong(), size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios = memScoped {
        val termios = alloc<termios>()
        if (tcgetattr(STDIN_FILENO, termios.ptr) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        return Termios(
            iflag = termios.c_iflag.convert(),
            oflag = termios.c_oflag.convert(),
            cflag = termios.c_cflag.convert(),
            lflag = termios.c_lflag.convert(),
            cc = ByteArray(NCCS) { termios.c_cc[it].convert() },
        )
    }

    override fun setStdinTermios(termios: Termios): Unit = memScoped {
        val nativeTermios = alloc<termios>()
        // different platforms have different fields in termios, so we need to read the current
        // struct before we set the fields we carre about.
        if (tcgetattr(STDIN_FILENO, nativeTermios.ptr) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        nativeTermios.c_iflag = termios.iflag.convert()
        nativeTermios.c_oflag = termios.oflag.convert()
        nativeTermios.c_cflag = termios.cflag.convert()
        nativeTermios.c_lflag = termios.lflag.convert()
        repeat(NCCS) { nativeTermios.c_cc[it] = termios.cc[it].convert() }
        if (tcsetattr(platform.posix.STDIN_FILENO, TCSADRAIN, nativeTermios.ptr) != 0) {
            throw RuntimeException("Error setting terminal attributes")
        }
    }

    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char = memScoped {
        do {
            val c = alloc<ByteVar>()
            val read = read(platform.posix.STDIN_FILENO, c.ptr, 1u)
            if (read < 0) throw RuntimeException("Error reading from stdin")
            if (read > 0) return c.value.toInt().toChar()
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
