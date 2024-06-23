package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.internal.Size
import kotlinx.cinterop.*
import platform.posix.*

// The source code for this file is identical between linux and apple targets, but they have different
// bit widths for some the termios fields, so the compileMetadata task fails.

internal actual fun getTerminalSizeNative(): Size? = memScoped {
    val size = alloc<winsize>()
    if (ioctl(STDIN_FILENO, TIOCGWINSZ.toULong(), size) < 0) {
        null
    } else {
        Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
    }
}

internal actual fun getStdinTermiosNative(): SyscallHandlerPosix.Termios = memScoped {
    val termios = alloc<termios>()
    if (tcgetattr(STDIN_FILENO, termios.ptr) != 0) {
        throw RuntimeException("Error reading terminal attributes")
    }
    return SyscallHandlerPosix.Termios(
        iflag = termios.c_iflag.convert(),
        oflag = termios.c_oflag.convert(),
        cflag = termios.c_cflag.convert(),
        lflag = termios.c_lflag.convert(),
        cc = ByteArray(NCCS) { termios.c_cc[it].convert() },
    )
}

internal actual fun setStdinTermiosNative(termios: SyscallHandlerPosix.Termios): Unit = memScoped {
    val nativeTermios = alloc<termios>()
    // different platforms have different fields in termios, so we need to read the current
    // struct before we set the fields we care about.
    if (tcgetattr(STDIN_FILENO, nativeTermios.ptr) != 0) {
        throw RuntimeException("Error reading terminal attributes")
    }
    nativeTermios.c_iflag = termios.iflag.convert()
    nativeTermios.c_oflag = termios.oflag.convert()
    nativeTermios.c_cflag = termios.cflag.convert()
    nativeTermios.c_lflag = termios.lflag.convert()
    repeat(NCCS) { nativeTermios.c_cc[it] = termios.cc[it].convert() }
    if (tcsetattr(STDIN_FILENO, TCSADRAIN, nativeTermios.ptr) != 0) {
        throw RuntimeException("Error setting terminal attributes")
    }
}
