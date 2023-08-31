package com.github.ajalt.mordant.internal

import kotlinx.cinterop.*
import platform.posix.*

// https://www.gnu.org/software/libc/manual/html_node/getpass.html
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun ttySetEcho(echo: Boolean) = memScoped {
    val termios = alloc<termios>()
    if (tcgetattr(STDOUT_FILENO, termios.ptr) != 0) {
        return@memScoped
    }

    termios.c_lflag = if (echo) {
        termios.c_lflag or ECHO.convert()
    } else {
        termios.c_lflag and ECHO.inv().convert()
    }

    tcsetattr(0, TCSAFLUSH, termios.ptr)
}
